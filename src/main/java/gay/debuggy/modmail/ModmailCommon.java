package gay.debuggy.modmail;

import java.util.Set;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

/**
 * @author woodiertexas
 * @author Ampflower
 * @author falkreon
 * @since v1.0.0
 **/
public class ModmailCommon {
	// Constants
	public static final String whiteCheckMark = "U+2705";
	public static final int lightRed = 0xf54058;
	public static final int lightGreen = 0x2ac48e;
	
	private static final Set<String> HARMFUL_EXTENSIONS = Set.of(
		".sh", ".exe", ".scr", ".bat", ".vbs",
		".cmd", ".msi", ".com", ".efi", ".o"
	);
	
	// Methods
	/**
	 * Checks if an url ends with a forbidden extension.
	 *
	 * @param url The URL to check.
	 * @return boolean
	 */
	public static boolean isHarmful(String url) {
		int extPos = url.lastIndexOf('.');
		if (extPos == -1) return false;
		
		String ext = url.substring(extPos, url.length());
		
		return (HARMFUL_EXTENSIONS.contains(ext));
	}
	
	/**
	 * Creates an EmbedBuilder and fills in details representing a User - for instance, for an embed inserted into a
	 * modmail thread in response to a DM to the bot.
	 * 
	 * @param user The user to represent in the embed
	 * @return the EmbedBuilder, which can be further customized and then built.
	 */
	public static EmbedBuilder createEmbedBuilder(User user) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(user.getName(), null, user.getAvatarUrl())
			.setDescription(user.getAsMention() + " registered their account <t:" + user.getTimeCreated().toEpochSecond() + ":R>")
			.addField("Registered @", "<t:" + user.getTimeCreated().toEpochSecond() + ">", true)
			.addField("User ID", user.getId(), true)
			.setColor(lightGreen);
		return result;
	}
	
	/**
	 * Creates an EmbedBuilder and fills in details representing a Guild - for instance, the guild that the modmail bot
	 * is running in, for use in official DMs back to the affected user.
	 * 
	 * @param guild The guild to represent in the embed
	 * @return the EmbedBuilder, which can be further customized and then built.
	 */
	public static EmbedBuilder createEmbedBuilder(Guild guild) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(guild.getName(), null, guild.getIconUrl())
			.setColor(lightGreen);
		return result;
	}
}
