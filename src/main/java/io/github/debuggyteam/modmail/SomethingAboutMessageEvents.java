package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author woodiertexas
 * @since ${version}
 **/
public class SomethingAboutMessageEvents extends ListenerAdapter {

	@Override
	public void onMessageReceived(final MessageReceivedEvent msgEvent) {
		if (msgEvent.isFromType(ChannelType.PRIVATE)) {
			handleMessage(msgEvent, Main.targetChannel);
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

		final var theMessage = msgEvent.getMessage();
		final var theUser = msgEvent.getMessage().getAuthor();
		final List<Message.Attachment> theAttachments = theMessage.getAttachments();

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
}
