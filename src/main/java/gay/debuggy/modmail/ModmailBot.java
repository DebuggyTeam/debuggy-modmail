package gay.debuggy.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
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
			final Message theMessage = msgEvent.getMessage();
			final EmbedBuilder embedBuilder = new EmbedBuilder();
			final Guild guild = targetChannel.getGuild();
			final User theUser = msgEvent.getMessage().getAuthor();
			List<String> executableUrls = new ArrayList<>();
			boolean executableFound = false;

			for (int i = 0; i < theMessage.getAttachments().size(); i++) {
				if (ModmailCommon.isExecutable(theMessage.getAttachments().get(i).getUrl())) {
					executableFound = true;
					executableUrls.add(theMessage.getAttachments().get(i).getUrl());
				}
			}

			if (!executableFound) {
				boolean doesThreadExist = modmailThread.containsKey(theUser.getIdLong());
				// ModmailCommon.createCommonEmbed(embedBuilder, guild.getName(), guild.getIconUrl(), "Would you like to create a modmail thread?", 0);
				// theMessage.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
				// handleMessage(msgEvent, targetChannel);

				if (!doesThreadExist) {
					ModmailCommon.createCommonEmbed(embedBuilder, theUser.getAsTag(), theUser.getAvatarUrl(), theUser.getAsMention() + " registered their account <t:" + theUser.getTimeCreated().toEpochSecond() + ":R>", ModmailCommon.lightGreen);
					embedBuilder.addField("Registered @", "<t:" + theUser.getTimeCreated().toEpochSecond() + ">", true);
					embedBuilder.addField("User ID", theUser.getId(), true);

					targetChannel.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
						message.getChannel().asTextChannel().createThreadChannel(theUser.getName() + "'s thread", message.getId()).queue(threadChannel -> {
							modmailThread.put(theUser.getIdLong(), threadChannel.getIdLong());
							threadToUser.put(threadChannel.getIdLong(), (PrivateChannel) msgEvent.getChannel());
							handleMessage(msgEvent, threadChannel);

							threadChannel.sendMessage("<@&931245994128048191> <:yeefpineapple:1096590659814686720>").queue();
						});
					});
				} else {
					handleMessage(msgEvent, client.getThreadChannelById(modmailThread.get(theUser.getIdLong())));
				}
			} else {
				ModmailCommon.createCommonEmbed(embedBuilder, theUser.getName(), theUser.getAvatarUrl(), theMessage.getContentRaw(), ModmailCommon.alertRed);
				embedBuilder.addField("Executable link(s) found:", String.valueOf(executableUrls), false);
				targetChannel.sendMessageEmbeds(embedBuilder.build()).queue();

				embedBuilder.clear();
				ModmailCommon.createCommonEmbed(embedBuilder, guild.getName(), guild.getIconUrl(), "Your latest message contains one or more executable files. Please do not send executables in modmail.", ModmailCommon.alertRed);
				theMessage.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
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
		final EmbedBuilder embedBuilder = new EmbedBuilder();

		if (messageEditEvent.isFromType(ChannelType.PRIVATE)) {
			ThreadChannel theThread = null;
			for (Long threadId : modmailThread.values()) {
				theThread = client.getThreadChannelById(threadId);
			}

			ModmailCommon.createCommonEmbed(embedBuilder, theUser.getName(), theUser.getAvatarUrl(), "Edited message: " + theMessage.getContentRaw(), ModmailCommon.lightGreen);
			embedBuilder.addField("User ID", theUser.getId(), true);
			assert theThread != null;
			theThread.sendMessageEmbeds(embedBuilder.build()).queue();
		}
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashEvent) {
		final Guild guild = targetChannel.getGuild();
		final EmbedBuilder embedBuilder = new EmbedBuilder();
		final MessageChannel theChannel = slashEvent.getMessageChannel();

		if (slashEvent.getName().equals("close")) {
			if (slashEvent.getChannel() instanceof ThreadChannel) {
				ModmailCommon.createCommonEmbed(embedBuilder, guild.getName(), guild.getIconUrl(), "Do you want to close this thread?", ModmailCommon.lightGreen);
				slashEvent.replyEmbeds(embedBuilder.build()).addActionRow(
					Button.primary("yes", "Yes"),
					Button.primary("no", "No")
				).setEphemeral(true).queue();
			} else {
				theChannel.sendMessage("<:yeefpineapple:1096590659814686720>").queue();
			}
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
		//final Guild guild = targetChannel.getGuild();
		//final EmbedBuilder embedBuilder = new EmbedBuilder();
		//boolean successful;

		if (buttonEvent.getComponentId().equals("yes")) {
			long modmailThreadId = 0;
			for (Long threadId : modmailThread.values()) {
				modmailThreadId = client.getThreadChannelById(threadId).getIdLong();
			}

			try {
				closeThreadAndNotifyUser(modmailThreadId, buttonEvent.getUser());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			buttonEvent.editMessage("thread closed").queue();
		} else if (buttonEvent.getComponentId().equals("no")) {
			buttonEvent.editMessage("thread stays open").queue();
		}
	}

	private void closeThreadAndNotifyUser(long modmailThreadId, @NotNull User threadCloser) throws InterruptedException {
		client.awaitReady();

		final EmbedBuilder embedBuilder = new EmbedBuilder();
		ThreadChannel theThread = client.getThreadChannelById(modmailThreadId);
		PrivateChannel dmChannel = threadToUser.get(modmailThreadId);

		if (theThread == null) {
			return;
		}

		// Send a "thread closed" message in the thread itself and to the thread creator.
		ModmailCommon.createCommonEmbed(
			embedBuilder,
			theThread.getGuild().getName(),
			theThread.getGuild().getIconUrl(),
			threadCloser.getName() + " has closed this thread.",
			ModmailCommon.lightGreen
		);
		embedBuilder.setFooter(threadCloser.getId() + " • Staff");

		dmChannel.sendMessageEmbeds(embedBuilder.build()).queue();
		theThread.sendMessageEmbeds(embedBuilder.build()).queue(message -> {
			theThread.getManager().setArchived(true).queue();
		});

		theThread.retrieveParentMessage().queue(message -> {
			message.editMessage("This thread is now closed.").queue(message1 -> {
			});
		});

		// Finally remove the modmail thread from the hashmaps.

		modmailThread.remove(dmChannel.getUser().getIdLong(), modmailThreadId);
	}

	/**
	 * Proxies the message to the given channel.
	 *
	 * @param msgEvent      The message to proxy
	 * @param targetChannel Where to send the message.
	 */
	private static void handleMessage(MessageReceivedEvent msgEvent, MessageChannel targetChannel) {
		final EmbedBuilder embedBuilder = new EmbedBuilder();
		final Message theMessage = msgEvent.getMessage();
		final User theUser = msgEvent.getMessage().getAuthor();
		final List<Message.Attachment> theAttachments = theMessage.getAttachments();

		if (theMessage.getAuthor().getIdLong() == botId) {
			return;
		}

		boolean spoilEmbeds = false;
		final var attachmentList = new ArrayList<String>(theAttachments.size());

		embedBuilder.setAuthor(theUser.getName(), theUser.getAvatarUrl(), theUser.getAvatarUrl());
		embedBuilder.setFooter(theUser.getId() + " • Staff");

		if (msgEvent.getChannel() instanceof ThreadChannel) {
			if (theMessage.getContentRaw().startsWith("re: ")) {
				embedBuilder.setDescription(theMessage.getContentRaw().substring(4));
				embedBuilder.setColor(ModmailCommon.lightGreen);
				theMessage.addReaction(Emoji.fromUnicode(ModmailCommon.whiteCheckMark)).queue();
			} else {
				return;
			}
		} else {
			embedBuilder.setDescription(theMessage.getContentRaw());
			embedBuilder.setColor(ModmailCommon.lightGreen);
			theMessage.addReaction(Emoji.fromUnicode(ModmailCommon.whiteCheckMark)).queue();
		}

		final var itr = theAttachments.iterator();

		// Finds first image to embed.
		while (itr.hasNext()) {
			final var attachment = itr.next();

			if (attachment.isImage()) {
				// This only is needed for images.
				// Everything else can be spoiled via pipes.
				spoilEmbeds = attachment.isSpoiler();

				embedBuilder.setImage(attachment.getUrl());

				// No need to continue here; we found the first image.
				break;
			} else {
				appendAttachment(attachmentList, attachment);
			}
		}

		var builder = targetChannel.sendMessageEmbeds(embedBuilder.build());

		// Reuse builder
		embedBuilder.clear();

		while (itr.hasNext()) {
			final var attachment = itr.next();

			if (attachment.isImage()) {
				spoilEmbeds |= attachment.isSpoiler();
				embedBuilder.setImage(attachment.getUrl());
				builder.addEmbeds(embedBuilder.build());
			} else {
				appendAttachment(attachmentList, attachment);
			}
		}


		if (!attachmentList.isEmpty()) {
			embedBuilder.clear();
			embedBuilder.setTitle("Additional attachments");

			final var descriptionBuilder = embedBuilder.getDescriptionBuilder();
			for (final var attachment : attachmentList) {
				descriptionBuilder.append(attachment).append('\n');
			}

			builder.addEmbeds(embedBuilder.build());
		}

		if (spoilEmbeds) {
			builder.setContent("The message sent by ``" + theUser.getName() + "`` had attachments as spoilers. ||http://./||");
		}

		builder.queue();
	}

	private static void appendAttachment(List<String> attachmentList, Message.Attachment attachment) {
		if (attachment.isSpoiler()) {
			attachmentList.add("||" + attachment.getUrl() + "||");
		} else {
			attachmentList.add(attachment.getUrl());
		}
	}
}
