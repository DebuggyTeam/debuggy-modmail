package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import static io.github.debuggyteam.modmail.Main.*;

/**
 * @author woodiertexas
 * @author Ampflower
 * @since ${version}
 **/
public class SomethingAboutMessageEvents extends ListenerAdapter {

	@Override
	public void onMessageReceived(final MessageReceivedEvent msgEvent) {
		if (msgEvent.isFromType(ChannelType.PRIVATE)) {
			final Message theMessage = msgEvent.getMessage();
			List<String> executableUrls = new ArrayList<>();
			boolean executableFound = false;

			for (int i = 0; i < theMessage.getAttachments().size(); i++) {

				if (isExecutable(theMessage.getAttachments().get(i).getUrl())) {
					executableFound = true;
					executableUrls.add(theMessage.getAttachments().get(i).getUrl());
				}
			}

			if (!executableFound) {
				handleMessage(msgEvent, targetChannel);
			}

			if (executableFound) {
				final EmbedBuilder theEmbed = new EmbedBuilder();
				final Guild guild = targetChannel.getGuild();
				final User theUser = msgEvent.getMessage().getAuthor();

				createCommonEmbed(theEmbed, theUser.getName(), theUser.getAvatarUrl(), theMessage.getContentRaw(), 16711680);
				theEmbed.addField("Executable link(s) found:", String.valueOf(executableUrls), false);
				targetChannel.sendMessageEmbeds(theEmbed.build()).queue();

				theEmbed.clear();
				createCommonEmbed(theEmbed, guild.getName(), guild.getIconUrl(), "Your latest message contains one or more executable files. Please do not send executables in modmail.", 16711680);
				theMessage.getChannel().sendMessageEmbeds(theEmbed.build()).queue();

			}
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

		theEmbed.setAuthor(theUser.getName(), theUser.getAvatarUrl(), theUser.getAvatarUrl());
		theEmbed.setFooter(theUser.getId() + " • Community Manager");
		theEmbed.setDescription(theMessage.getContentRaw());

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

	/**
	 * Checks if a url ends with an executable extension.
	 *
	 * @param url The URL to check.
	 * @return boolean
	 */
	boolean isExecutable(String url) {
		String[] listOfExtensions = {
			".sh", ".exe", ".scr", ".bat", ".vsb", ".cmd"
		};

		for (String extension : listOfExtensions) {
			if (url.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a common embed.
	 *
	 * @param embed         The EmbedBuilder to use.
	 * @param authorName    The author's name (can be a user, a guild, or something else).
	 * @param authorIconUrl The author's icon URL (can be a user, a guild, or something else).
	 * @param description   The text description.
	 * @param color         The embed's bar color.
	 */
	void createCommonEmbed(EmbedBuilder embed, String authorName, String authorIconUrl, String description, int color) {
		embed.setAuthor(authorName, authorIconUrl, authorIconUrl);
		embed.setDescription(description);
		embed.setColor(color);
	}
}
