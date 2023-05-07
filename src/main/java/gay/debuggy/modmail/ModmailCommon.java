package gay.debuggy.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;

public class ModmailCommon {
	// Constants
	static final String whiteCheckMark = "U+2705";
	static final int alertRed = 0xFF0000;
	static final int lightGreen = 0x2ac48e;


	/**
	 * Checks if a url ends with an executable extension.
	 *
	 * @param url The URL to check.
	 * @return boolean
	 */
	static boolean isExecutable(String url) {
		String[] listOfExtensions = {
			".sh", ".exe", ".scr", ".bat", ".vbs",
			".cmd", ".msi", ".com", ".efi", ".o"
		};

		for (String extension : listOfExtensions) {
			if (url.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates a common embed.
	 *
	 * @param embed         The EmbedBuilder to use.
	 * @param authorName    The author's name (can be a user, a guild, or something else).
	 * @param authorIconUrl The author's icon URL (can be a user, a guild, or something else).
	 * @param description   The text description.
	 * @param color         The embed's bar color.
	 */
	static void createCommonEmbed(EmbedBuilder embed, String authorName, String authorIconUrl, String description, int color) {
		embed.setAuthor(authorName, authorIconUrl, authorIconUrl);
		embed.setDescription(description);
		embed.setColor(color);
	}

	static void setEmbedAuthor(EmbedBuilder embed, User user) {
		embed.setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl());
		embed.setColor(ModmailCommon.lightGreen);
	}


	static EmbedBuilder createEmbedBuilder(User user) {
		EmbedBuilder result = new EmbedBuilder();
		result
			.setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl())
    		.setColor(lightGreen)
			.addField("registered @", user.getTimeCreated().toEpochSecond() + ":R>", true)
    		.addField("User ID", user.getId(), true);
		return result;
	}
}
