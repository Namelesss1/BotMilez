package commands.trivia.triviaeditor;

import commands.IBotCommand;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class TriviaEditorCommand extends ListenerAdapter implements IBotCommand {

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

        if (event.getUser().hasPrivateChannel()) {
            event.getChannel().sendMessage("Cannot start session, " +
                            "you already have an active trivia editing session! ")
                    .queue();
            return;
        }

        event.reply("Alright, lets start the process. I DM'd you instructions.")
                .setEphemeral(true)
                .queue();

        TriviaEditSession session = new TriviaEditSession(event.getUser());
    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {

    }



}
