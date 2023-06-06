package gay.debuggy.modmail;

import gay.debuggy.modmail.modules.MemberCommands;
import gay.debuggy.modmail.modules.MemberScreening;
import gay.debuggy.modmail.modules.ModMail;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Objects;

/**
 * @author Ampflower
 * @since v1.0.0
 **/
public final class Main {
	/**
	 * MODULES is where you would add the modules you want to load when the bot runs.
	 */
	private static final ListenerAdapter[] MODULES = {new ModMail(), new MemberScreening(), new MemberCommands()};

	private static final String token = System.getenv("__DEBUGGY_MODMAIL_TOKEN");
	private static final String channel = System.getenv("__DEBUGGY_MODMAIL_TARGET");

	public static JDA client;
	public static GuildMessageChannel targetChannel;
	public static long botId;

	static {
		Objects.requireNonNull(token, "Set environment __DEBUGGY_MODMAIL_TOKEN to your bot's token.");
		Objects.requireNonNull(channel, "Set environment __DEBUGGY_MODMAIL_TARGET to your chosen channel.");
	}

	public static void main(String[] args) throws InterruptedException {
		final var builder = JDABuilder.createLight(token)
			.enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
			.setMemberCachePolicy(MemberCachePolicy.ALL)
			// Even though createLight should already disallow these, I'm putting these here anyways.
			.disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.SCHEDULED_EVENTS)
			.setActivity(Activity.of(Activity.ActivityType.WATCHING, "for your DMs!"));

		for (ListenerAdapter module : MODULES) {
			builder.addEventListeners(module);
		}

		client = builder.build().awaitReady();
		targetChannel = client.getTextChannelById(channel);
		botId = client.getSelfUser().getIdLong();

		if (targetChannel == null) {
			System.err.println("""
				Set environment __DEBUGGY_MODMAIL_TARGET to a valid channel.

				It is currently set to the following:\s""" + channel);

			System.exit(1);
		}

		Guild debuggyCord = client.getGuildById(912349224232943649L);



		Objects.requireNonNull(debuggyCord).updateCommands().addCommands(
			Commands.slash("close", "close a modmail thread"),
			Commands.slash("v", "Apply for verification in Debuggy."),
			Commands.slash("nickname", "Edit nickname.")
				.addOption(OptionType.STRING, "name", "Enter desired nickname.")
		).queue();

		/*
		debuggyCord.retrieveCommands().queue(commands -> {
			commands.forEach(command -> command.delete().queue());
		});

		 */
	}
}
