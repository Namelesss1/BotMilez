package commands.rng;

import commands.IBotCommand;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class RngCommand implements IBotCommand {

    private List<OptionData> options;
    private final String MAX_OPTION = "upper_bound";
    private final String MIN_OPTION = "lower_bound";

    public RngCommand() {
        options = new ArrayList<>();

        options.add(
                new OptionData(OptionType.INTEGER, MAX_OPTION,
                        "Highest possible number to generate",
                        false)
                        .setMaxValue(Integer.MAX_VALUE - 1));

        options.add(
                new OptionData(OptionType.INTEGER, MIN_OPTION,
                        "Lowest possible number to generate",
                        false)
                        .setMinValue(1));

    }

    @Override
    public String getName() {
        return "rng";
    }

    @Override
    public String getDesc() {
        return "Generate a random number within range specified by options (inclusive) or" +
                " between 0 and 2147483647";
    }

    @Override
    public List<OptionData> getOptions() {
        return options;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

        int max = Integer.MAX_VALUE;
        int min = 0;

        boolean maxSpecified = event.getOption(MAX_OPTION) != null;
        boolean minSpecified = event.getOption(MIN_OPTION) != null;

        Random rand = new Random();


        if (!maxSpecified && !minSpecified) {
            event.reply(Integer.toString(rand.nextInt(max))).queue();
        }

        else if (!minSpecified) {
            max = event.getOption(MAX_OPTION).getAsInt() + 1; /* To make upper bound inclusive */
            event.reply(Integer.toString(rand.nextInt(max))).queue();
        }

        else if (!maxSpecified) {
            min = event.getOption(MIN_OPTION).getAsInt();
            event.reply(Integer.toString(rand.nextInt(max - min) + min)).queue();
        }

        /* Meaning both max and min were specified i.e. an explicit range */
        else {
            max = event.getOption(MAX_OPTION).getAsInt() + 1;
            min = event.getOption(MIN_OPTION).getAsInt();

            if (min > max) {
                event.reply("Error: Lower bound cannot be more than upper bound").queue();
                return;
            }

            event.reply(Integer.toString(rand.nextInt(max - min) + min)).queue();
        }

    }
}
