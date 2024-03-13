package commands.trivia;

import commands.IBotCommand;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
    private final String OPTION_TRIVIA_NAME = "name";

    /* maximum umber of questions option */
    private final String OPTION_TRIVIA_MAX_QUESTIONS = "max_questions";

    /* Maximum points to win option */
    private final String OPTION_TRIVIA_MAX_POINTS = "max_points";

    /* Max time per question in seconds option */
    private final String OPTION_TRIVIA_SECONDS_PER_Q = "seconds_per_question";

    /* Channels in which active trivia games are happening */
    private final List<Long> activeTrivias;

    public TriviaCommand() {
        activeTrivias = new ArrayList<>();
        options = new ArrayList<>();

        options.add(
                new OptionData(OptionType.STRING, OPTION_TRIVIA_NAME,
                        "tag to identify which trivias to load",
                        true, true));
        options.add(
                new OptionData(OptionType.INTEGER, OPTION_TRIVIA_MAX_QUESTIONS,
                        "Maximum number of questions to send before ending trivia (up to 50)",
                        false, true));
        options.add(
                new OptionData(OptionType.INTEGER, OPTION_TRIVIA_MAX_POINTS,
                        "Number of points a player needs to win (up to 100)",
                        false, true));
        options.add(
                new OptionData(OptionType.INTEGER, OPTION_TRIVIA_SECONDS_PER_Q,
                        "Time you have to guess the answer in seconds",
                        false, true));

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

        if (activeTrivias.contains(event.getChannel().getIdLong())) {
            event.reply("There is already an active ongoing trivia!" +
                    " Please wait until current on finishing in this channel")
                    .queue();
            return;
        }

        activeTrivias.add(event.getChannel().getIdLong());

        String tag = event.getOption(OPTION_TRIVIA_NAME).getAsString();
        int maxQuestions = 40;
        int maxPoints = 50;
        int questionTime = 15;
        if (event.getOption(OPTION_TRIVIA_MAX_QUESTIONS) != null) {
            maxQuestions = event.getOption(OPTION_TRIVIA_MAX_QUESTIONS).getAsInt();
        }
        if (event.getOption(OPTION_TRIVIA_MAX_POINTS) != null) {
            maxPoints = event.getOption(OPTION_TRIVIA_MAX_POINTS).getAsInt();
        }
        if (event.getOption(OPTION_TRIVIA_SECONDS_PER_Q) != null) {
            questionTime = event.getOption(OPTION_TRIVIA_SECONDS_PER_Q).getAsInt();
        }

        event.reply("Now preparing the trivia game...").queue();

        Trivia triviaInstance =
                new Trivia(tag, maxQuestions, maxPoints, questionTime, event.getChannel(), event.getUser(),
                        this);

        triviaInstance.start();

    }

    @Override
    public void getHelp(StringSelectInteractionEvent event) {

    }


    /**
     * Removes a channel from the list of channels with an active trivia
     * @param channelId channel to remove
     */
    public void removeChannelFromActive(long channelId) {
        activeTrivias.remove(channelId);
    }
}
