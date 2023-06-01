package gay.debuggy.modmail.modules;

import gay.debuggy.modmail.ModmailCommon;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

import static gay.debuggy.modmail.Main.client;
import static gay.debuggy.modmail.Main.targetChannel;

public class MemberScreening extends ListenerAdapter {
	private static final long verifiedRole = 1106021960070205462L;

	@Override
	public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent slashEvent) {
		if (slashEvent.getName().equals("v")) {
			TextInput findOut = TextInput.create("find_out", "How did you find out about Debuggy?", TextInputStyle.PARAGRAPH)
				.setPlaceholder("I found out about Debuggy through the Quilt Community server.")
				.setMinLength(10)
				.build();

			TextInput whyJoin = TextInput.create("why_join", "Why are you joining Debuggy?", TextInputStyle.PARAGRAPH)
				.setPlaceholder("To get help with a Debuggy project.")
				.setMinLength(10)
				.build();

			TextInput pronouns = TextInput.create("pronouns", "Enter your preferred pronouns", TextInputStyle.SHORT)
				.setRequired(false)
				.build();

			Modal modal = Modal.create("membership_screening", "Membership Screening")
				//.addActionRows(ActionRow.of(findOut), ActionRow.of(pronouns))
				.addComponents(ActionRow.of(findOut), ActionRow.of(whyJoin), ActionRow.of(pronouns))
				.build();

			slashEvent.replyModal(modal).queue();
		}
	}

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		if (event.getModalId().equals("membership_screening")) {
			String pronouns = event.getValue("pronouns").getAsString();
			String findOut = event.getValue("find_out").getAsString();
			User user = event.getUser();

			//event.getMember().modifyNickname(user.getName() + " (" + pronouns + ")").queue();
			event.reply("Thanks for applying.").setEphemeral(true).queue();

			if (pronouns.isEmpty()) {
				pronouns = "Prefer not to say.";
			}

			EmbedBuilder verifyEmbed = new EmbedBuilder();
			verifyEmbed
				.setAuthor("New membership application")
				.addField("Username", user.getName(), true)
				.addField("User ID", user.getId(), true)
				.addField(user.getName() + "'s pronouns", pronouns, true)
				.addField("How did " + user.getName() + " find out about Debuggy?", findOut, false);

			targetChannel.sendMessageEmbeds(verifyEmbed.build()).addActionRow(
				Button.success("accept", "Accept"),
				Button.danger("refuse", "Deny")
			).queue();
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent buttonEvent) {
		String value = ModmailCommon.getValueFromEmbedField(buttonEvent.getMessage(), 0, 1);
		String pronouns = ModmailCommon.getValueFromEmbedField(buttonEvent.getMessage(), 0, 2);
		User user = client.getUserById(value);
		Message message = buttonEvent.getMessage();
		Guild guild = buttonEvent.getGuild();

		if (buttonEvent.getComponentId().equals("accept")) {
			/*
			try {
				if (!pronouns.isEmpty()) {
					guild.getMemberById(user.getId()).modifyNickname(user.getName() + " (" + pronouns + ")").queue();
				}
			} catch (HierarchyException e) {
				buttonEvent.reply("I can't edit the nickname of " + user.getName()).setEphemeral(true).queue();
				message.editMessageComponents().queue();
				return;
			}

			 */
			guild.addRoleToMember(user, client.getRoleById(verifiedRole)).queue();

			MessageEmbed acceptedEmbed = ModmailCommon.createEmbedBuilder(guild)
				.setDescription("Hello " + user.getName() + ", your application to join Debuggy Community has been accepted." +
					"\n\nWe also encourage you to put your preferred pronouns into your nickname." +
					"The easiest way to do that is type in `/nick` then paste in `" + user.getName() + " (" + pronouns + ")`.")
				.build();

			user.openPrivateChannel().queue(dmChannel -> {
				dmChannel.sendMessageEmbeds(acceptedEmbed).queue();
			});

			EmbedBuilder acceptedColor = new EmbedBuilder(message.getEmbeds().get(0));
			acceptedColor.setColor(ModmailCommon.lightGreen);
			message.editMessageEmbeds(acceptedColor.build()).queue();

			buttonEvent.reply("The user's join application has been accepted.").setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
		} else if (buttonEvent.getComponentId().equals("refuse")) {
			user.openPrivateChannel().queue(dmChannel -> {
				dmChannel.sendMessage("Hello " + user.getName() + ", your application to join Debuggy Community has been denied.").queue();
			});

			EmbedBuilder deniedColor = new EmbedBuilder(message.getEmbeds().get(0));
			deniedColor.setColor(ModmailCommon.lightRed);
			message.editMessageEmbeds(deniedColor.build()).queue();

			buttonEvent.reply("The user's join application has been denied.").setEphemeral(true).queueAfter(1, TimeUnit.SECONDS);
		}

		message.editMessageComponents().queue();
	}
}
