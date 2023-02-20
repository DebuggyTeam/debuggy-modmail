package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 * @author Ampflower
 * @since ${version}
 **/
public final class Main {
	//private static final String token = System.getenv("__DEBUGGY_MODMAIL_TOKEN");

	public static JDA client;

	public static void main(String[] args) {
		//TODO: Don't include the token
		final var builder = JDABuilder.createLight("bleh")
			.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
			.setActivity(Activity.of(Activity.ActivityType.WATCHING, "for your DMs!"))
			// Even though createLight should already disallow these, I'm putting these here anyways.
			.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
				.addEventListeners(new SomethingAboutMessageEvents());
		
		client = builder.build();
	}
}
