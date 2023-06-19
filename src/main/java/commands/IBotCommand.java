package commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * An interface specifying the operations that a command must implement,
 * including its name, description, its functionality, and information
 * about what arguments it takes and how many arguments were provided.
 */
public interface IBotCommand {


    /**
     * Get the command name
     *
     * @return String containing command's name
     */
    public String getName();



    /**
     * Get the description of the command
     *
     * @return a string containing the description of the command
     */
    public String getDesc();


    /**
     * Get a list of options that this command supports
     *
     * @return list of options supported by the command
     */
    public List<OptionData> getOptions();




    /**
     * Perform the action specified by the command and its arguments, if any.
     *
     * @param event the event that triggered the slash command
     */
    public void doAction(SlashCommandInteractionEvent event);

}
