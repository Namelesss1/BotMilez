package commands;
import commands.basic.BubbleWrapCommand;
import commands.basic.HiCommand;
import commands.rng.RngCommand;
import commands.rng.RngUsername;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getUser().isBot()) {
            super.onSlashCommandInteraction(event);

            String cmdName = event.getName();
            for (IBotCommand command : getBotCommands()) {
                if (cmdName.equals(command.getName())) {
                    command.doAction(event);
                }
            }
        }

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {

        /* To contain the commands used by bot */
        List<CommandData> commands = new ArrayList<>();

        for (IBotCommand command : getBotCommands()) {
              List<OptionData> options = command.getOptions();

              CommandCreateAction action =
                      event.getJDA().upsertCommand(command.getName(), command.getDesc()).setGuildOnly(true);

              if (options != null) {
                  action.addOptions(options);
              }

              action.queue();

        }

    }

    /**
     *
     * @return An array containing all of the bot's available commands
     */
    private static IBotCommand[] getBotCommands() {
        IBotCommand commands[] = {
                new HiCommand(),
                new RngCommand(),
                new RngUsername(),
                new BubbleWrapCommand(),
        };

        return commands;

    }

}
