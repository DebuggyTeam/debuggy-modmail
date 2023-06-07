package gay.debuggy.modmail.modules;

import gay.debuggy.modmail.ModmailCommon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static gay.debuggy.modmail.Main.client;
import static gay.debuggy.modmail.Main.targetChannel;
import static gay.debuggy.modmail.ModmailCommon.getElapsedTimeInMilliseconds;

public class MemberCommands extends ListenerAdapter {
	final long WEEK_IN_MILLISECONDS = TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS);

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashEvent) {
		if (slashEvent.getName().equals("nickname")) {
			Member member = slashEvent.getMember();
			String name = slashEvent.getOption("name").getAsString();
			long memberJoined = member.getTimeJoined().toEpochSecond();

			if (name.length() > 32) {
				slashEvent.reply("`" + name + "` is over the maximum 32 character limit for Discord nicknames.").setEphemeral(true).queue();
				return;
			}

			if (getElapsedTimeInMilliseconds(TimeUnit.SECONDS.toMillis(memberJoined), System.currentTimeMillis()) < WEEK_IN_MILLISECONDS) {
				slashEvent.reply("Your nickname request has been submitted.").setEphemeral(true).queue();

				MessageEmbed nicknameRequestEmbed = ModmailCommon.createEmbedBuilder(slashEvent.getGuild())
					.setTitle("Nickname Request")
					.addField("Current Name", member.getNickname(), true)
					.addField("Requested Name", name, true)
					.addField("User ID", member.getId(), true)
					.build();

				targetChannel.sendMessageEmbeds(nicknameRequestEmbed).addActionRow(
					Button.success("approve", "Approve"),
					Button.danger("refuse", "Deny")
				).queue();
				return;
			}

			try {
				member.modifyNickname(name).queue();
				slashEvent.reply("Your nickname was updated to " + name).setEphemeral(true).queue();
			} catch (HierarchyException e) {
				slashEvent.reply("You have to set your nickname yourself. Here's the name you entered: `" + name + "`").setEphemeral(true).queue();
			}
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
		Guild guild = buttonEvent.getGuild();
		Message message = buttonEvent.getMessage();
		String name = ModmailCommon.getValueFromEmbedField(buttonEvent.getMessage(), 0, 0);
		String id = ModmailCommon.getValueFromEmbedField(buttonEvent.getMessage(), 0, 1);
		User user = client.getUserById(id);

		if (buttonEvent.getComponentId().equals("approve")) {
			EmbedBuilder acceptedColor = new EmbedBuilder(message.getEmbeds().get(0));
			acceptedColor.setColor(ModmailCommon.LIGHT_GREEN);
			message.editMessageEmbeds(acceptedColor.build()).queue();

			try {
				guild.modifyNickname((Member) user, name);
				buttonEvent.reply("Updated " + ((Member) user).getNickname() + "'s name.").setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
			} catch (HierarchyException e) {
				buttonEvent.reply("Couldn't update " + ((Member) user).getNickname() + "'s name.").setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
			}
		} else if (buttonEvent.getComponentId().equals("refuse")) {
			user.openPrivateChannel().queue(dmChannel -> {
				dmChannel.sendMessage("Hello " + user.getName() + ", Debuggy staff has denied changing your nickname to ." + name).queue();
			});

			EmbedBuilder deniedColor = new EmbedBuilder(message.getEmbeds().get(0));
			deniedColor.setColor(ModmailCommon.LIGHT_RED);
			message.editMessageEmbeds(deniedColor.build()).queue();

			buttonEvent.reply("The user's nickname request has been denied.").setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
		}

		message.editMessageComponents().queue();
	}
}
