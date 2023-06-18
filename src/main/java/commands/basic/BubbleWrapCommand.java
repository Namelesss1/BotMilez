package commands.basic;

import commands.IBotCommand;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.ArrayList;

public class BubbleWrapCommand implements IBotCommand{

    private final List<OptionData> options;
    private final String OPTION_SIZE_NAME = "size";
    private final int MAX_SIZE = 7;
    private final int MIN_SIZE = 1;

    private int size = 5;

    public BubbleWrapCommand() {
        options = new ArrayList<>();

        options.add(
                new OptionData(OptionType.INTEGER, OPTION_SIZE_NAME,
                        "width by length of bubble wrap, max " + MAX_SIZE,
                        false, true));
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

        if (event.getOption(OPTION_SIZE_NAME) != null) {
            size = event.getOption(OPTION_SIZE_NAME).getAsInt();
        }

        StringBuilder msg = new StringBuilder();

        if (size >= MIN_SIZE && size <= MAX_SIZE) {
             for (int row = 0; row < size; row++) {
                 for (int col = 0; col < size; col++) {
                     msg.append("||pop!||");
                 }
                 msg.append("\n");
             }
             event.reply(msg.toString()).queue();
             return;
        }

        event.reply("Error: Size must be less than " + MAX_SIZE +
                " and greater than " + MIN_SIZE).queue();
    }



}
