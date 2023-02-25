package io.github.debuggyteam.modmail;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static io.github.debuggyteam.modmail.Main.targetChannel;

public class TestCommand extends ListenerAdapter {

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashEvent) {
		final Guild guild = targetChannel.getGuild();
		final EmbedBuilder theEmbed = new EmbedBuilder();
		final User theUser = slashEvent.getUser();

		if (slashEvent.getName().equals("test")) {
			theEmbed.setAuthor(theUser.getAsTag(), theUser.getAvatarUrl(), theUser.getAvatarUrl());
			theEmbed.setDescription(theUser.getAsMention() + " registered their account <t:" + theUser.getTimeCreated().toEpochSecond() + ":R>");
			theEmbed.addField("Registered @", "<t:" + theUser.getTimeCreated().toEpochSecond() + ">", true);
			theEmbed.addField("User ID", theUser.getId(), true);


			targetChannel.sendMessageEmbeds(theEmbed.build()).queue(message -> {
				message.getChannel().asTextChannel().createThreadChannel(theUser.getName() + "'s thread", message.getId()).queue();
			});
		}
	}
}
