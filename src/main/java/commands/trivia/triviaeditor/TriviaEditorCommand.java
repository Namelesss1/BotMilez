package commands.trivia.triviaeditor;

import commands.IBotCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class TriviaEditorCommand extends ListenerAdapter implements IBotCommand {

    private static List<String> activeEditIDs;

    public TriviaEditorCommand() {
        activeEditIDs = new ArrayList<>();
    }

    @Override
    public String getName() {
        return "trivia_edit";
    }

    @Override
    public String getDesc() {
        return "Create your own custom trivia, or edit one you made!";
    }

    @Override
    public List<OptionData> getOptions() {
        return null;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

        if (activeEditIDs.contains(event.getUser().getId())) {
            event.getChannel().sendMessage("Cannot start session, " +
                    "you already have an active trivia editing session! ")
                    .queue();
            return;

        }

        event.reply("Check your dms. Instructions to editing your trivia are there to " +
                        "prevent other users from seeing information of it such as" +
                        " answers to your questions.")
                .setEphemeral(true)
                .queue();

        TriviaEditSession session = new TriviaEditSession(event.getUser());
    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {

    }

    /**
     * Adds an active user to the list of active ones
     * @param id id of channel to add
     */
    public static void addToActiveUser(String id) {
        activeEditIDs.add(id);
    }

    public static void removeActiveUsers(String id) {
        activeEditIDs.remove(id);
    }
}
