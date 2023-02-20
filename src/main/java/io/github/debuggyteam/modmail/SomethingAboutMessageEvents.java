package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.EventListener;

public class SomethingAboutMessageEvents extends EventListener {
    
    public void onMessageRecieved(MessageReceivedEvent msgEvent) {
        if (msgEvent.isFromType(ChannelType.PRIVATE)) {
            msgEvent.getChannel().sendMessage("wut").queue();
        }
    }
    
    public static void init() {};
}
