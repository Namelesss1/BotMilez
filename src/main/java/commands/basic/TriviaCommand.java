package commands.basic;

import commands.IBotCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

/**
 * =============== TriviaCommand ===============
 *
 * Allows users to play a round of trivia. Questions are based on
 * the categories that a user has chosen when starting the trivia round.
 * Players gain a point for being the first person to answer correctly.
 * If no one answers correctly by the time limit, move to the next question.
 * Amount of questions asked is configurable when starting a trivia round.
 *
 * In the future, will also allow users to create their own custom trivia
 * questions and answers.
 *
 */
public class TriviaCommand implements IBotCommand {

    /* List of options that can be used with command e.g. trivia type, create new */
    private final List<OptionData> options;

    /* name of option to choose which trivia to play */
    private final String OPTION_NAME_TRIVIA = "name";

    public TriviaCommand() {
        options = new ArrayList<>();

        options.add(
                new OptionData(OptionType.STRING, OPTION_NAME_TRIVIA,
                        "name of trivia to play",
                        true, true));
    }

    @Override
    public String getName() {
        return "trivia";
    }

    @Override
    public String getDesc() {
        return "Compete with other users in a game of trivia! " +
                "Many are available, and you can also create your own.";
    }

    @Override
    public List<OptionData> getOptions() {
        return options;
    }

    @Override
    public void doAction(SlashCommandInteractionEvent event) {

    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {

    }
}
