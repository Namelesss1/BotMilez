package commands.helper;

import commands.IBotCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class HelpCommand implements IBotCommand {

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDesc() {
        return "Get help on how to use certain commands";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

    }

}
