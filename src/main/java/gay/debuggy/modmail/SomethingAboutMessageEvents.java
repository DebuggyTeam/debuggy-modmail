package gay.debuggy.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static gay.debuggy.modmail.Main.*;

/**
 * @author woodiertexas
 * @author Ampflower
 * @since ${version}
 **/
public class SomethingAboutMessageEvents extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(SomethingAboutMessageEvents.class);

	public Map<Long, Long> modmailThread = new ConcurrentHashMap<>();
	public Map<Long, MessageChannel> threadToUser = new ConcurrentHashMap<>();

	@Override
	public void onMessageReceived(final MessageReceivedEvent msgEvent) {
		// Exclude bots from here; they are not to be proxied normally.
		if (msgEvent.getAuthor().isBot()) {
			return;
		}

		if (msgEvent.isFromType(ChannelType.PRIVATE)) {
			final Message theMessage = msgEvent.getMessage();
			final EmbedBuilder theEmbed = new EmbedBuilder();
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
				// ModmailCommon.createCommonEmbed(theEmbed, guild.getName(), guild.getIconUrl(), "Would you like to create a modmail thread?", 0);
				// theMessage.getChannel().sendMessageEmbeds(theEmbed.build()).queue();
				// handleMessage(msgEvent, targetChannel);

				if (!doesThreadExist) {
					ModmailCommon.createCommonEmbed(theEmbed, theUser.getAsTag(), theUser.getAvatarUrl(), theUser.getAsMention() + " registered their account <t:" + theUser.getTimeCreated().toEpochSecond() + ":R>", 0x000000);
					theEmbed.addField("Registered @", "<t:" + theUser.getTimeCreated().toEpochSecond() + ">", true);
					theEmbed.addField("User ID", theUser.getId(), true);

					targetChannel.sendMessageEmbeds(theEmbed.build()).queue(message -> {
						message.getChannel().asTextChannel().createThreadChannel(theUser.getName() + "'s thread", message.getId()).queue(threadChannel -> {
							modmailThread.put(theUser.getIdLong(), threadChannel.getIdLong());
							threadToUser.put(threadChannel.getIdLong(), msgEvent.getChannel());
							handleMessage(msgEvent, threadChannel);

							threadChannel.sendMessage("<@&931245994128048191> <:yeefpineapple:1096590659814686720>").queue();
						});
					});
				} else {
					handleMessage(msgEvent, client.getThreadChannelById(modmailThread.get(theUser.getIdLong())));
				}
			} else {
				ModmailCommon.createCommonEmbed(theEmbed, theUser.getName(), theUser.getAvatarUrl(), theMessage.getContentRaw(), 0xFF0000);
				theEmbed.addField("Executable link(s) found:", String.valueOf(executableUrls), false);
				targetChannel.sendMessageEmbeds(theEmbed.build()).queue();



				theEmbed.clear();
				ModmailCommon.createCommonEmbed(theEmbed, guild.getName(), guild.getIconUrl(), "Your latest message contains one or more executable files. Please do not send executables in modmail.", 0xFF0000);
				theMessage.getChannel().sendMessageEmbeds(theEmbed.build()).queue();

			}
		} else if (msgEvent.getChannelType().isThread()) {
			final var fromChannel = msgEvent.getChannel();
			final var userChannel = threadToUser.get(fromChannel.getIdLong());

			if (userChannel != null) {
				handleMessage(msgEvent, userChannel);
			}
		}
	}

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashEvent) {
		final Guild guild = targetChannel.getGuild();
		final EmbedBuilder theEmbed = new EmbedBuilder();
		final User theUser = slashEvent.getUser();
		final MessageChannel theChannel = slashEvent.getMessageChannel();

		if (slashEvent.getName().equals("close")) {
			if (slashEvent.getChannel() instanceof ThreadChannel) {
				ModmailCommon.createCommonEmbed(theEmbed, guild.getName(), guild.getIconUrl(), "Do you want to close this thread?", 0x000000);
				theChannel.sendMessageEmbeds(theEmbed.build()).addActionRow(
					Button.primary("yes", "Yes"),
					Button.primary("no", "No")
				).queue();
			} else {
				theChannel.sendMessage("<:yeefpineapple:1096590659814686720>").queue();
			}
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
		final MessageChannel theChannel = buttonEvent.getMessageChannel();
		if (buttonEvent.getComponentId().equals("yes")) {
			for (Map.Entry<Long, Long> entry : modmailThread.entrySet()) {
				Long userId = entry.getKey();
				Long threadId = entry.getValue();

				if (threadId.equals(theChannel.getIdLong())) {
					modmailThread.remove(userId, threadId);
					threadToUser.remove(threadId);
				}
			}
			buttonEvent.editMessage("thread closed").queue();
		} else if (buttonEvent.getComponentId().equals("no")) {
			buttonEvent.editMessage("thread stays open").queue();
		}
	}

	/**
	 * Proxies the message to the given channel.
	 *
	 * @param msgEvent      The message to proxy
	 * @param targetChannel Where to send the message.
	 */
	private static void handleMessage(MessageReceivedEvent msgEvent, MessageChannel targetChannel) {
		final EmbedBuilder theEmbed = new EmbedBuilder();

		final Message theMessage = msgEvent.getMessage();
		final User theUser = msgEvent.getMessage().getAuthor();
		final List<Message.Attachment> theAttachments = theMessage.getAttachments();

		if (theMessage.getAuthor().getIdLong() == botId) {
			return;
		}

		boolean spoilEmbeds = false;
		final var attachmentList = new ArrayList<String>(theAttachments.size());

		//theMessage.addReaction(Objects.requireNonNull(client.getEmojiById(1097391615661838488L))).queue();
		theEmbed.setAuthor(theUser.getName(), theUser.getAvatarUrl(), theUser.getAvatarUrl());
		theEmbed.setFooter(theUser.getId() + " • Staff");

		if (msgEvent.getChannel() instanceof ThreadChannel) {
			if (theMessage.getContentRaw().startsWith("re: ")) {
				theEmbed.setDescription(theMessage.getContentRaw().substring(4));
			} else {
				return;
			}
		} else {
			theEmbed.setDescription(theMessage.getContentRaw());
		}

		final var itr = theAttachments.iterator();

		// Finds first image to embed.
		while (itr.hasNext()) {
			final var attachment = itr.next();

			if (attachment.isImage()) {
				// This only is needed for images.
				// Everything else can be spoiled via pipes.
				spoilEmbeds = attachment.isSpoiler();

				theEmbed.setImage(attachment.getUrl());

				// No need to continue here; we found the first image.
				break;
			} else {
				appendAttachment(attachmentList, attachment);
			}
		}

		var builder = targetChannel.sendMessageEmbeds(theEmbed.build());

		// Reuse builder
		theEmbed.clear();

		while (itr.hasNext()) {
			final var attachment = itr.next();

			if (attachment.isImage()) {
				spoilEmbeds |= attachment.isSpoiler();
				theEmbed.setImage(attachment.getUrl());
				builder.addEmbeds(theEmbed.build());
			} else {
				appendAttachment(attachmentList, attachment);
			}
		}


		if (!attachmentList.isEmpty()) {
			theEmbed.clear();
			theEmbed.setTitle("Additional attachments");

			final var descriptionBuilder = theEmbed.getDescriptionBuilder();
			for (final var attachment : attachmentList) {
				descriptionBuilder.append(attachment).append('\n');
			}

			builder.addEmbeds(theEmbed.build());
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
