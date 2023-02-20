package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

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
                
                theEmbed.setAuthor(theUser.getName(), theUser.getAvatarUrl(), theUser.getAvatarUrl());
                theEmbed.setFooter(theUser.getId() + " • Community Manager");
                theEmbed.setDescription(theMessage.getContentRaw());
                
                
                if (!theAttachments.isEmpty()) {
                    for (Message.Attachment theAttachment : theAttachments) {
                        theEmbed.setImage(theAttachment.getUrl());
                    }
                }
                
                Objects.requireNonNull(Main.client.getTextChannelById(1077094694485512273L)).sendMessageEmbeds(theEmbed.build()).queue();
            }
        }
    }
}
