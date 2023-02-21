package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author woodiertexas
 * @since ${version}
 **/
public class SomethingAboutMessageEvents implements EventListener {

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        EmbedBuilder theEmbed = new EmbedBuilder();

        if (event instanceof MessageReceivedEvent msgEvent) {
            if (msgEvent.isFromType(ChannelType.PRIVATE)) {
				var theMessage = msgEvent.getMessage();
				var theUser = msgEvent.getMessage().getAuthor();
				List<Message.Attachment> theAttachments = theMessage.getAttachments();

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

				var builder = Main.targetChannel.sendMessageEmbeds(theEmbed.build());

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
		}
	}

	private static void appendAttachment(List<String> attachmentList, Message.Attachment attachment) {
		if (attachment.isSpoiler()) {
			attachmentList.add("||" + attachment.getUrl() + "||");
		} else {
			attachmentList.add(attachment.getUrl());
		}
	}
}
