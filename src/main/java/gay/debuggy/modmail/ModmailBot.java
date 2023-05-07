package gay.debuggy.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static gay.debuggy.modmail.Main.botId;
import static gay.debuggy.modmail.Main.client;
import static gay.debuggy.modmail.Main.targetChannel;

/**
 * @author woodiertexas
 * @author Ampflower
 * @since ${version}
 **/
public class ModmailBot extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(ModmailBot.class);

	public Map<Long, Long> modmailThread = new ConcurrentHashMap<>();
	public Map<Long, PrivateChannel> threadToUser = new ConcurrentHashMap<>();

	@Override
	public void onMessageReceived(final MessageReceivedEvent msgEvent) {
		// Exclude bots from here; they are not to be proxied normally.
		if (msgEvent.getAuthor().isBot()) {
			return;
		}

		if (msgEvent.isFromType(ChannelType.PRIVATE)) {
			final Message message = msgEvent.getMessage();
			final User sender = msgEvent.getMessage().getAuthor();
			
			
			// Scan for threats
			List<String> harmfulUrls = new ArrayList<>();
			boolean threatFound = false;

			for (int i = 0; i < message.getAttachments().size(); i++) {
				if (ModmailCommon.isHarmful(message.getAttachments().get(i).getUrl())) {
					threatFound = true;
					harmfulUrls.add(message.getAttachments().get(i).getUrl());
				}
			}
			
			if (threatFound) {
				
				//Notify mods
				MessageEmbed modmailThreadMessage = ModmailCommon.createEmbedBuilder(sender)
						.addField("Potentially harmful link(s) found:", "`" + String.valueOf(harmfulUrls) + "`", false)
						.appendDescription(message.getContentRaw())
						.setColor(ModmailCommon.alertRed)
						.build();
				
				//Send the notice to the modmail channel if applicable, otherwise to the bot channel
				ThreadChannel modmailChannel = getModmailThread(sender);
				if (modmailChannel!=null) {
					modmailChannel.sendMessageEmbeds(modmailThreadMessage).queue();
				} else {
					targetChannel.sendMessageEmbeds(modmailThreadMessage).queue();
				}
				
				//Notify sender
				MessageEmbed replyMessage = ModmailCommon.createEmbedBuilder(targetChannel.getGuild())
						.appendDescription("Your latest message contains one or more ptentially harmful files. Please do not send executables in modmail.")
						.setColor(ModmailCommon.alertRed)
						.build();
				
				message.getChannel().sendMessageEmbeds(replyMessage).queue();
				
				return;
			}
			
			
			// The message is clean. Is this a new thread or should we proxy the message to their existing modmail thread?
			ThreadChannel modmailThread = getModmailThread(sender);
			
			if (modmailThread == null) {
				//This is a new thread.
				/*
				MessageEmbed embed = ModmailCommon.createEmbedBuilder(targetChannel.getGuild())
					.appendDescription("Would you like to create a modmail thread?")
					.setColor(0)
					.build();
			
				message.getChannel().sendMessageEmbeds(embed).queue();
				*/
				
				//Create the new thread and update the maps
				
				MessageEmbed embed = ModmailCommon.createEmbedBuilder(sender).build();
				targetChannel.sendMessageEmbeds(embed).queue(threadParent -> {
					if (targetChannel instanceof TextChannel textChannel) {
						textChannel
							.createThreadChannel(sender.getName()+"'s thread", threadParent.getId())
							.queue(threadChannel -> {
								
								//Log the new stuff
								this.modmailThread.put(sender.getIdLong(), threadChannel.getIdLong());
								threadToUser.put(threadChannel.getIdLong(), msgEvent.getChannel().asPrivateChannel());
								
								//Proxy the message into the new thread
								handleMessage(msgEvent, threadChannel);
								
								//Pineapple
								threadChannel.sendMessage("<@&931245994128048191> <:yeefpineapple:1096590659814686720>").queue();
							});
					} else {
						targetChannel.sendMessage("Could not create the thread! (targetChannel is not a text channel)");
					}
					
				});
				
			} else {
				//Just proxy the message
				
				handleMessage(msgEvent, modmailThread);
			}
			
		} else if (msgEvent.getChannelType().isThread()) {
			final var fromChannel = msgEvent.getChannel();
			final var userChannel = threadToUser.get(fromChannel.getIdLong());

			if (userChannel != null) {
				handleMessage(msgEvent, userChannel);
			}
		}
	}

	// Detect edited messages and relay them to the modmail thread.
	@Override
	public void onMessageUpdate(MessageUpdateEvent messageEditEvent) {
		final Message theMessage = messageEditEvent.getMessage();
		final User theUser = messageEditEvent.getMessage().getAuthor();

		if (messageEditEvent.isFromType(ChannelType.PRIVATE)) {
			//Find the modmail thread
			ThreadChannel modmailThread = getModmailThread(theUser);
			
			if (modmailThread == null) {
				theMessage.reply("<:yeefpineapple:1096590659814686720><:x:>").queue(); //TODO: Again, maybe better feedback
				return;
				
			} else {
				//Create the embed
				EmbedBuilder embedBuilder = ModmailCommon.createEmbedBuilder(theUser)
						.appendDescription("Edited message: " + theMessage.getContentRaw());
				
				//Send it!
				modmailThread.sendMessageEmbeds(embedBuilder.build()).queue();
			}
		}
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashEvent) {
		if (slashEvent.getName().equals("close")) {
			if (slashEvent.getChannel() instanceof ThreadChannel) {
				
				MessageEmbed embed = ModmailCommon.createEmbedBuilder(targetChannel.getGuild())
					.appendDescription("Do you want to close this thread?")
					.build();
				
				slashEvent
					.replyEmbeds(embed)
					.addActionRow(
						Button.primary("yes", "Yes"),
						Button.primary("no", "No")
					)
					.setEphemeral(true)
					.queue();
					
				
			} else {
				slashEvent
					.getMessageChannel()
					.sendMessage("<:yeefpineapple:1096590659814686720>")
					.queue();
			}
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {

		if (buttonEvent.getComponentId().equals("yes")) {
			MessageChannelUnion buttonChannel = buttonEvent.getChannel();
			if (buttonChannel instanceof ThreadChannel thread) {
				
				closeThreadAndNotifyUser(thread.getIdLong(), buttonEvent.getUser()); //Assume these buttons were for closing the thread
				buttonEvent.editMessage("Thread closed.").queue();
			} else {
				buttonEvent.editMessage("Error closing the thread.").queue();
			}

		} else if (buttonEvent.getComponentId().equals("no")) {
			buttonEvent.editMessage("Thread stays open.").queue();
		}
	}

	/**
	 * Closes the modmail thread, and sends an embed message to both the thread-creator and the thread-closer letting
	 * them know that the thread has been closed.
	 * @param modmailThreadId The thread to close
	 * @param threadCloser The user who is closing the thread.
	 * @return true if the thread was successfully closed. false indicates a consistency error in the internal maps
	 */
	private boolean closeThreadAndNotifyUser(long modmailThreadId, @NotNull User threadCloser) {
		
		// Grab data and check consistency. Failures in these checks represent serious internal errors!
		
		PrivateChannel dmChannel = threadToUser.get(modmailThreadId);
		if (dmChannel == null) {
			return false; //TODO: Throw a fit
		}

		ThreadChannel thread = getModmailThread(dmChannel.getUser());
		if (thread==null || thread.getIdLong() != modmailThreadId) {
			return false; //TODO: Also throw a fit.
		}

		// Create a "Thread closed" message
		MessageEmbed embed = ModmailCommon
			.createEmbedBuilder(targetChannel.getGuild())
			.appendDescription(threadCloser.getName() + " has closed this thread.")
			.setFooter(threadCloser.getId() + " • Staff")
			.build();

		// Send the embed to both the thread-closer and the thread-creator
		dmChannel.sendMessageEmbeds(embed).queue();
		thread.sendMessageEmbeds(embed).queue(message -> {
			thread.getManager().setArchived(true).queue();
		});

		thread.retrieveParentMessage().queue(message -> {
			message.editMessage("This thread is now closed.").queue();
		});

		// Finally remove the modmail thread from the hashmaps.
		modmailThread.remove(dmChannel.getUser().getIdLong(), modmailThreadId);
		threadToUser.remove(modmailThreadId, dmChannel.getUser().getIdLong());
		
		return true;
	}

	/**
	 * Proxies the message to the given channel.
	 *
	 * @param msgEvent      The message to proxy
	 * @param targetChannel Where to send the message.
	 */
	private static void handleMessage(MessageReceivedEvent msgEvent, MessageChannel targetChannel) {
		final Message theMessage = msgEvent.getMessage();
		final User theUser = msgEvent.getMessage().getAuthor();
		final List<Message.Attachment> theAttachments = theMessage.getAttachments();

		if (theMessage.getAuthor().getIdLong() == botId) {
			return;
		}

		boolean spoilEmbeds = false;
		
		EmbedBuilder embedBuilder = new EmbedBuilder()
				.setAuthor(theUser.getName(), theUser.getAvatarUrl(), theUser.getAvatarUrl())
				.setFooter(theUser.getId() + " • Staff");

		if (msgEvent.getChannel() instanceof ThreadChannel) {
			if (theMessage.getContentRaw().startsWith("re: ")) {
				embedBuilder
						.setDescription(theMessage.getContentRaw().substring(4))
						.setColor(ModmailCommon.lightGreen);
				theMessage.addReaction(Emoji.fromUnicode(ModmailCommon.whiteCheckMark)).queue();
			} else {
				return;
			}
		} else {
			embedBuilder
					.setDescription(theMessage.getContentRaw())
					.setColor(ModmailCommon.lightGreen);
			theMessage.addReaction(Emoji.fromUnicode(ModmailCommon.whiteCheckMark)).queue();
		}

		//Set the image of the embed to the first attached image
		Message.Attachment previewImage = null;
		for(Message.Attachment attachment : theMessage.getAttachments()) {
			if (attachment.isImage()) {
				spoilEmbeds = attachment.isSpoiler();
				embedBuilder.setImage(attachment.getUrl());
				previewImage = attachment;
				break;
			}
		}

		MessageCreateAction messageCreateAction = targetChannel.sendMessageEmbeds(embedBuilder.build());
		
		// If there were additional attachments, create a new embed to contain them
		if ((previewImage == null && theMessage.getAttachments().size() > 0) || theMessage.getAttachments().size() > 1) {
			
			// Reuse builder
			embedBuilder.clear();
			embedBuilder.setTitle("Additional attachments");
			final var descriptionBuilder = embedBuilder.getDescriptionBuilder();
			
			for(Message.Attachment attachment : theMessage.getAttachments()) {
				if (attachment == previewImage) continue; // We already embedded previewImage
					
				descriptionBuilder.append(attachment).append('\n');
			}
			
			messageCreateAction.addEmbeds(embedBuilder.build());
		}
		
		if (spoilEmbeds) {
			messageCreateAction.setContent("The message sent by ``" + theUser.getName() + "`` had attachments as spoilers. ||http://./||");
		}

		messageCreateAction.queue();
	}

	@Nullable
	private ThreadChannel getModmailThread(@NotNull User user) {
		final Long threadId = modmailThread.get(user.getIdLong());
		if (threadId == null) return null;
		
		@Nullable final ThreadChannel modmailThread = client.getThreadChannelById(threadId);
		return modmailThread; //May also be null
	}
	
	private static void appendAttachment(List<String> attachmentList, Message.Attachment attachment) {
		if (attachment.isSpoiler()) {
			attachmentList.add("||" + attachment.getUrl() + "||");
		} else {
			attachmentList.add(attachment.getUrl());
		}
	}
}
