package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Objects;

/**
 * @author Ampflower
 * @since ${version}
 **/
public final class Main {
	private static final String token = System.getenv("__DEBUGGY_MODMAIL_TOKEN");
	private static final String channel = System.getenv("__DEBUGGY_MODMAIL_TARGET");

	public static JDA client;
	public static GuildMessageChannel targetChannel;

	static {
		Objects.requireNonNull(token, "Set environment __DEBUGGY_MODMAIL_TOKEN to your bot's token.");
		Objects.requireNonNull(channel, "Set environment __DEBUGGY_MODMAIL_TARGET to your chosen channel.");
	}

	public static void main(String[] args) throws InterruptedException {
		final var builder = JDABuilder.createLight(token)
			.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
			.setActivity(Activity.of(Activity.ActivityType.WATCHING, "for your DMs!"))
			// Even though createLight should already disallow these, I'm putting these here anyways.
			.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
			.addEventListeners(new SomethingAboutMessageEvents());

		client = builder.build().awaitReady();

		targetChannel = client.getTextChannelById(channel);

		if (targetChannel == null) {
			System.err.println("""
				Set environment __DEBUGGY_MODMAIL_TARGET to a valid channel.

				It is currently set to the following:\s""" + channel);

			System.exit(1);
		}
	}
}
