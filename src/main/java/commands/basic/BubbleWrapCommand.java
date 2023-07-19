package commands.basic;

import commands.IBotCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/**
 * =============== BubbleWrapCommand ===============
 *
 * A simple command that outputs a grid of the string "pop!"
 * each string hidden by a discord spoiler tag. The user can
 * choose the size of the grid (# rows = #columns) up to a certain
 * size.
 *
 * This command was added mainly to get used to and test the waters with
 * OptionData and options used by Discord Slash Commands, and have users
 * input what they want as an option from the command.
 *
 */
public class BubbleWrapCommand implements IBotCommand{

    private final List<OptionData> options;
    private final String OPTION_SIZE_NAME = "size";
    private final int MAX_SIZE = 7;
    private final int MIN_SIZE = 1;

    private final int DEFAULT_SIZE = 5;

    public BubbleWrapCommand() {
        options = new ArrayList<>();

        options.add(
                new OptionData(OptionType.INTEGER, OPTION_SIZE_NAME,
                        "width by length of bubble wrap, max " + MAX_SIZE,
                        false, true)
                        .setMaxValue(MAX_SIZE)
                        .setMinValue(MIN_SIZE));
    }

    @Override
    public String getName() {
        return "bubblewrap";
    }

    public String getDesc() {
        return "Pop some bubble wrap!";
    }

    public List<OptionData> getOptions() {
        return options;
    }

    public void doAction(SlashCommandInteractionEvent event) {

        int size = DEFAULT_SIZE;
        if (event.getOption(OPTION_SIZE_NAME) != null) {
            size = event.getOption(OPTION_SIZE_NAME).getAsInt();
        }

        StringBuilder msg = new StringBuilder();


        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                msg.append("||pop!||");
            }
            msg.append("\n");
        }
        event.reply(msg.toString()).queue();

    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {
        String desc = "This command sends a NxN grid of the word \"pop!\" each encased in a \n" +
                "discord spoiler tag to simulate popping bubble wrap. This command\n" +
                "is nothing special or interesting, mainly made to get used to some aspects\n" +
                "of making this bot.";
        String args = "You can specify the size of the grid when typing the command.\n" +
                "Minimum size allowed is " + MIN_SIZE + " and maximum is " + MAX_SIZE + ".\n" +
                "If you don't specify a size, the default is " + DEFAULT_SIZE + "." +
                "You can specify a size like so:\n" +
                "```/" + getName() + " 5```";

        EmbedBuilder emBuilder = new EmbedBuilder();
        emBuilder.setTitle("/" + getName());
        emBuilder.setDescription(getDesc());
        emBuilder.setColor(Color.BLACK);
        emBuilder.setFooter("Ragequitting at Mobile Legends? Relax and pop some bubble wrap.");
        emBuilder.addField(new MessageEmbed.Field(
                "Description",
                desc,
                false));
        emBuilder.addField(new MessageEmbed.Field(
                "Grid Size",
                args,
                false
        ));

        event.editMessageEmbeds(emBuilder.build()).setComponents().queue();
    }



}
