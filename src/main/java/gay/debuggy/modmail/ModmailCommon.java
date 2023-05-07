package gay.debuggy.modmail;

import java.util.Set;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class ModmailCommon {
	// Constants
	static final String whiteCheckMark = "U+2705";
	static final int alertRed = 0xFF0000;
	static final int lightGreen = 0x2ac48e;

	private static final Set<String> HARMFUL_EXTENSIONS = Set.of(
		".sh", ".exe", ".scr", ".bat", ".vbs",
		".cmd", ".msi", ".com", ".efi", ".o"
	);
	
	/**
	 * Checks if a url ends with a forbidden extension.
	 *
	 * @param url The URL to check.
	 * @return boolean
	 */
	static boolean isHarmful(String url) {
		return (HARMFUL_EXTENSIONS.contains(url));
	}
	
	/**
	 * Creates an EmbedBuilder and fills in details representing a User - for instance, for an embed inserted into a
	 * modmail thread in response to a DM to the bot.
	 * @param user The user to represent in the embed
	 * @return the EmbedBuilder, which can be further customized and then built.
	 */
	static EmbedBuilder createEmbedBuilder(User user) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(user.getName(), null, user.getAvatarUrl())
			.setColor(lightGreen)
			.addField("registered @", "<t:" + user.getTimeCreated().toEpochSecond() + ":R>", true)
			.addField("User ID", user.getId(), true);
		return result;
	}
	
	/**
	 * Creates an EmbedBuilder and fills in details representing a Guild - for instance, the guild that the modmail bot
	 * is running in, for use in official DMs back to the affected user.
	 * @param guild The guild to represent in the embed
	 * @return the EmbedBuilder, which can be further customized and then built.
	 */
	static EmbedBuilder createEmbedBuilder(Guild guild) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(guild.getName(), null, guild.getIconUrl())
			.setColor(lightGreen);
		return result;
	}
}
