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
        long debuggyBotTest = 1077094694485512273L;
        
        if (event instanceof MessageReceivedEvent msgEvent) {
            if (msgEvent.isFromType(ChannelType.PRIVATE)) {
                var theMessage = msgEvent.getMessage();
                var theUser = msgEvent.getMessage().getAuthor();
                List<Message.Attachment> theAttachments = theMessage.getAttachments();
                
                theEmbed.setAuthor(theUser.getName(), theUser.getAvatarUrl(), theUser.getAvatarUrl());
                theEmbed.setFooter(theUser.getId() + " • Community Manager");
                theEmbed.setDescription(theMessage.getContentRaw());
                
                int size = theAttachments.size();
                
                if (theAttachments.isEmpty()) {
                    Objects.requireNonNull(Main.client.getTextChannelById(debuggyBotTest)).sendMessageEmbeds(theEmbed.build()).queue();
                } else if (size == 1) {
                    theEmbed.setImage(theAttachments.get(0).getUrl());
                    Objects.requireNonNull(Main.client.getTextChannelById(debuggyBotTest)).sendMessageEmbeds(theEmbed.build()).queue();
                } else if (size >= 2) {

                    var channel = Main.client.getTextChannelById(debuggyBotTest);
                    var builder = channel.sendMessageEmbeds(theEmbed.build());
                    
                    for (int i = 0; i < size; i++) {
                        theEmbed.setImage(theAttachments.get(i).getUrl());
                        
                        builder.addEmbeds(theEmbed.build());
                    }
                    
                    builder.queue();
                }
            }
        }
    }
}
