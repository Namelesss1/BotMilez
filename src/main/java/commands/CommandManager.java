package commands;
import commands.basic.BubbleWrapCommand;
import commands.basic.HiCommand;
import commands.quotes.QuoteCommand;
import commands.rng.RngCommand;
import commands.rng.RngUsername;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles methods that deal with commands that a user
 * inputs, initializes needed data for the commands,
 * and runs the appropriate command as specified by the user.
 */
public class CommandManager extends ListenerAdapter {

    private Map<String, IBotCommand> commands;

    public CommandManager() {
        /* Initialize command instances */
        commands = new HashMap<>();

        HiCommand hi_command = new HiCommand();
        RngCommand rng_command = new RngCommand();
        RngUsername rng_username_command = new RngUsername();
        BubbleWrapCommand bubblewrap_command = new BubbleWrapCommand();
        QuoteCommand quote_command = new QuoteCommand();

        commands.put(hi_command.getName(), hi_command);
        commands.put(rng_command.getName(), rng_command);
        commands.put(rng_username_command.getName(), rng_username_command);
        commands.put(bubblewrap_command.getName(), bubblewrap_command);
        commands.put(quote_command.getName(), quote_command);


    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        if (!event.getUser().isBot()) {
            super.onSlashCommandInteraction(event);

            String cmdName = event.getName();
            for (IBotCommand command : commands.values()) {
                if (cmdName.equals(command.getName())) {
                    command.doAction(event);
                }
            }
        }

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {

        for (IBotCommand command : commands.values()) {
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
     * @return A map where the values are commands, key's are the command's names.
     */
    public Map<String, IBotCommand> getCommands() {
        return commands;
    }

}
