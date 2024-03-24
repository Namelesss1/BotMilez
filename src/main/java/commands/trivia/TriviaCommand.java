package commands.trivia;

import commands.IBotCommand;
import commands.Stoppable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
public class TriviaCommand extends ListenerAdapter implements IBotCommand {

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
                        true, true)
        );
        options.add(
                new OptionData(OptionType.INTEGER, OPTION_TRIVIA_MAX_QUESTIONS,
                        "Maximum number of questions to send before ending trivia (up to 50)",
                        false, false)
                        .setMaxValue(50)
                        .setMinValue(10)
        );
        options.add(
                new OptionData(OptionType.INTEGER, OPTION_TRIVIA_MAX_POINTS,
                        "Number of points a player needs to win (up to 100)",
                        false, false)
                        .setMinValue(10)
                        .setMaxValue(100)
        );
        options.add(
                new OptionData(OptionType.INTEGER, OPTION_TRIVIA_SECONDS_PER_Q,
                        "Time you have to guess the answer in seconds",
                        false, false)
                        .setMaxValue(30)
                        .setMinValue(5)
        );

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
        int maxQuestions = 15;
        int maxPoints = 25;
        int questionTime = 20;
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
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle(getName());
        builder.setDescription(getDesc());
        builder.setFooter("Time to show the world what you know");
        builder.setColor(Color.MAGENTA);

        builder.addField(
                "Options",
                "name: This is mandatory. This determines what kinds of questions" +
                        " will be sent during the game. The name of the trivia to play if only" +
                        " one strict trivia is desired. If you want multiple trivia categories" +
                        " in a game, enter a tag that identifies a multiple trivias that share that tag. " +
                        " For a very interesting game of trivia, you can load a mix of different questions types" +
                        " from any trivia that is available on the server you're playing in. To do this, " +
                        " type ALL as the name." +
                        "\n\n" +
                        OPTION_TRIVIA_MAX_POINTS + ": optional. The maximum amount of points a player can" +
                        " earn before the trivia ends. The default is 30" +
                        "\n\n" +
                        OPTION_TRIVIA_MAX_QUESTIONS + ": optional. The maximum amount of questions that can " +
                        "be asked by the trivia. The default is 20 questions" +
                        OPTION_TRIVIA_SECONDS_PER_Q + ": optional. The time limit in seconds before the next " +
                        "question is asked if no one is getting the current one correct. The default is " +
                        "15 seconds.",
                false
        );

        builder.addField(
                "Starting the game",
                "Type the command /" + getName() + "followed by the name/tags of the types of trivia" +
                        " questions you want, and any additional options (mentioned above) that you want. " +
                        "The trivia will then begin. Anyone who sends a message in the channel the trivia is" +
                        " happening in is automatically entered as a participant of the game. Only one trivia game" +
                        " can happen in the same channel at once.",
                false
        );

        builder.addField(
                "Playing the game",
                "The bot will ask a question. Players will type an answer. If a player is correct, " +
                        "the amount of points the question was worth will be added to their score." +
                        "If wrong, a small cooldown will apply to the player where they will be unable" +
                        "to answer. This is to prevent guess spamming. The next question is shown " +
                        "when someone gets the answer correct or the question's time limit is up." +
                        " Once the game ends, the final scoreboard is displayed. ",
                false

        );

        builder.addField(
                "Stopping the game",
                "The game ends when someone gets the maximum amount of points, all available questions " +
                        "in a trivia were asked, or the maximum chosen amount of questions were asked." +
                        " Whichever happens first. You can also forcefully end a trivia by typing " +
                        Stoppable.CANCEL + " or " + Stoppable.END + " or " + Stoppable.END + ".",
                false
        );

        builder.addField(
                "Creating custom Trivia questions",
                "Use the trivia_edit command. For more info, use /help and " +
                        "select the /trivia_edit command.",
                false
        );

        event.editMessageEmbeds(builder.build()).queue();
    }


    /**
     * Removes a channel from the list of channels with an active trivia
     * @param channelId channel to remove
     */
    public void removeChannelFromActive(long channelId) {
        activeTrivias.remove(channelId);
    }


    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (event.getName().equals(getName()) &&
                event.getFocusedOption().getName().equals(OPTION_TRIVIA_NAME)) {
            List<String> autoComplete = getAutoCompleteOptions("resources/trivia/custom/", event.getChannel());
            List<Command.Choice> options = autoComplete.stream()
                    .filter(name -> name.startsWith(event.getFocusedOption().getValue()))
                    .map(name -> new Command.Choice(name, name)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }


    private List<String> getAutoCompleteOptions(String path, MessageChannel channel) {

        List<String> triviaNames = new ArrayList<>();

        final FileNameExtensionFilter extensionFilter =
                new FileNameExtensionFilter("N/A", "json");
        File tDir = new File(path);
        for (File file : tDir.listFiles()) {
            if (extensionFilter.accept(file) && file.isFile()) {
                String fileName = file.getName();
                TriviaType type = new TriviaType(path + fileName, channel.getJDA());
                String trivName = type.getName();
                List<String> ids = type.getServers();
                boolean universal = type.isUniversal();
                boolean addTrivia = true;

                /* If trivia is not universal and trivia is not allowed in this server, don't add */
                if (!universal) {
                    if (ids.isEmpty() || !ids.contains(((TextChannel)channel).getGuild().getId())) {
                        addTrivia = false;
                    }
                }

                if (addTrivia) {
                    if (type.getSize() > 0) {
                        triviaNames.add(trivName);
                    }
                }

            }
        }

        return triviaNames;
    }
}
