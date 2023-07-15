package commands;
import commands.basic.BubbleWrapCommand;
import commands.basic.HiCommand;
import commands.quotes.QuoteCommand;
import commands.rng.RngCommand;
import commands.rng.RngMkwCommand;
import commands.rng.RngUsernameCommand;
import commands.util.MkwStatsCommand;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;


import java.util.ArrayList;
import java.util.List;

/**
 * This class handles methods that deal with commands that a user
 * inputs, initializes needed data for the commands,
 * and runs the appropriate command as specified by the user.
 */
public class CommandManager extends ListenerAdapter {

    private List<IBotCommand> commands;

    public CommandManager() {
        /* Initialize command instances */
        commands = new ArrayList<>();

        commands.add(new HiCommand());
        commands.add(new RngCommand());
        commands.add(new RngUsernameCommand());
        commands.add(new BubbleWrapCommand());
        commands.add(new QuoteCommand());
        commands.add(new MkwStatsCommand());
        commands.add(new RngMkwCommand());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getUser().isBot()) {
            super.onSlashCommandInteraction(event);

            String cmdName = event.getName();
            for (IBotCommand command : commands) {
                if (cmdName.equals(command.getName())) {
                    command.doAction(event);
                }
            }
        }

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {

        for (IBotCommand command : commands) {
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
     * @return A list of all the commands in the bot.
     */
    public List<IBotCommand> getCommands() {
        return commands;
    }

}
