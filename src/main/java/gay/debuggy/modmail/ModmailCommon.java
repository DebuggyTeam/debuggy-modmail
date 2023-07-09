package gay.debuggy.modmail;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

/**
 * @author Ampflower
 * @author falkreon
 * @author woodiertexas
 * @since v1.0.0
 **/
public class ModmailCommon {
	// Constants
	public static final String WHITE_CHECK_MARK = "U+2705";
	public static final int LIGHT_RED = 0xf54058;
	public static final int LIGHT_GREEN = 0x2ac48e;
	public static final int DEBUGGY_BLUE = 0x3138bc;

	private static final Set<String> HARMFUL_EXTENSIONS = Set.of(
		".sh", ".exe", ".scr", ".bat", ".vbs",
		".cmd", ".msi", ".com", ".efi", ".o",
		".jar", ".pdf"
	);

	// Methods
	/**
	 * Checks if an url ends with a forbidden extension.
	 *
	 * @param url The URL to check
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
	 * @return the EmbedBuilder, which can be further customized and then built
	 */
	public static EmbedBuilder createEmbedBuilder(User user) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(user.getName(), null, user.getAvatarUrl())
			.setDescription(user.getAsMention() + " registered their account <t:" + user.getTimeCreated().toEpochSecond() + ":R>")
			.addField("Registered @", "<t:" + user.getTimeCreated().toEpochSecond() + ">", true)
			.addField("User ID", user.getId(), true)
			.setColor(DEBUGGY_BLUE);
		return result;
	}

	/**
	 * Creates an EmbedBuilder and fills in details representing a Guild - for instance, the guild that the modmail bot
	 * is running in, for use in official DMs back to the affected user.
	 *
	 * @param guild The guild to represent in the embed
	 * @return the EmbedBuilder, which can be further customized and then built
	 */
	public static EmbedBuilder createEmbedBuilder(Guild guild) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(guild.getName(), null, guild.getIconUrl())
			.setColor(DEBUGGY_BLUE);
		return result;
	}

	/**
	 * Gets a value from the specified embed field.
	 *
	 * @param message The message that contains an embed
	 * @param embedIndex The index for the embed
	 * @param fieldIndex The index for a field in an embed
	 * @return the field's value
	 */
	public static String getValueFromEmbedField(Message message, int embedIndex, int fieldIndex) {
		return message.getEmbeds().get(embedIndex).getFields().get(fieldIndex).getValue();
	}

	/**
	 * Gets the elapsed time between two timeframes.
	 *
	 * @param startTime The start time in milliseconds
	 * @param endTime The end time in milliseconds
	 * @return the elapsed time between `startTime` and `endTime`
	 */
	public static long getElapsedTimeInMilliseconds(long startTime, long endTime) {
		return endTime - startTime;
	}

	/*
	public static String redact(String string) {
		Pattern spoilers = Pattern.compile("\\|\\|(.*)\\|\\|");
		Matcher spoilerChecker = spoilers.matcher(string);

		Pattern contentWarning = Pattern.compile("cw: pii");
		Matcher contentWarningChecker = contentWarning.matcher(string);

		if () {
			return string;
		}
		return string;
	}

	 */
}
