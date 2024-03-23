package commands.trivia;

import commands.Stoppable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.IO;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class represents an instance of a currently-ongoing game of
 * trivia that was activated through the TriviaCommand.
 *
 */
public class Trivia extends ListenerAdapter implements Stoppable {

    /* Maximum allowed trivias being played on this bot at once */
    private static final int MAX_TRIVIA_COUNT = 10;

    /* Amount of currently active trivias */
    private static int triviaCount = 0;

    /* List of trivia types that are used in this trivia instance */
    private final List<TriviaType> triviaTypes;

    /* Maps of active players -> score in this trivia */
    private final Map<User, Long> playerToScore;

    /* Maximum number of questions to ask before ending trivia */
    private final int maxQuestions;

    /* Max amount of points a player can score to win trivia */
    private final long winningScore;

    /* Time limit for each question (in seconds) before moving on to next */
    private final int questionTimeLimit;

    /* Message Channel of which this trivia is happening in */
    private final MessageChannel channel;

    /* ID of channel this trivia is happening in */
    private long channelId;

    /* Path to the trivias */
    private static final String path = "resources/trivia/";

    /* Total number of questions in this trivia */
    private int numTotalQuestions;

    /* Number of questions asked thus far */
    private int numQuestionsAsked;

    /* Names of all trivias being played */
    private List<String> triviaNames;

    /* Given tag that a user specified */
    private String tag;

    /* Command which created this trivia object */
    private TriviaCommand command;

    /* previous question embed that was sent */
    private Message questionMsg;

    /*
     * An array of size two that helps keep track of which question is
     * currently active.
     * index [0]: index of which trivia type in triviaTypes list
     * index [1]: id of question within the trivia type
     */
    private int[] currentQuestionIndex;

    /* Timer that delays the next question being sent */
    private Timer questionDelayTimer;

    /* Main Question timer */
    private Timer questionTimer;

    /* Visual timer that the players see on display in an embed
     * to show how much time is left to answer a question
     */
    private Timer visualCountdownTimer;

    /* True if the trivia is ready to receive user responses. Otherwise, ignores them.
     * Used to prevent players from luckily guessing answers before a question is sent,
     * since a small intentional delay is added between questions.
     * Responses to end the trivia are still acknowledged.
     */
    private boolean readyToReceiveMessages;

    /*
     * Player cooldowns: When a player gets a question wrong, they
     * are added to this map and they cannot answer a question for a small amount
     * of time. To prevent guess spamming
     *
     * Map: User -> Timer representing how many seconds of cooldown left
     * If timer is null, then there is no cooldown.
     */
    private Map<User, Timer> playerCooldowns;


    /**
     * Constructor for an instance of a trivia game. Initializes needed information
     * for a trivia game including parameters below, and what questions will be asked
     * based on the given tag.
     *
     * @param tag used to filter what types of trivia questions to ask
     * @param maxQ maximum amount of questions to ask
     * @param maxPoints maximum amount of points a player can earn before winning
     * @param timeLimit amount of time in seconds before moving on to next question
     * @param channel MessageChannel this trivia is taking place in
     */
    public Trivia(String tag, int maxQ, int maxPoints, int timeLimit, MessageChannel channel,
                  User user, TriviaCommand triviaCommand) {
        this.tag = tag;
        maxQuestions = maxQ;
        winningScore = maxPoints;
        questionTimeLimit = timeLimit;
        this.channel = channel;
        channelId = channel.getIdLong();
        triviaTypes = new ArrayList<>();
        triviaNames = new ArrayList<>();

        currentQuestionIndex = new int[2];
        currentQuestionIndex[0] = -1;
        currentQuestionIndex[1] = -1;

        numTotalQuestions = 0;
        numQuestionsAsked = 0;

        /* Initialize player scores and cooldowns */
        playerToScore = new HashMap<>();
        playerToScore.put(user, new Long(0));
        playerCooldowns = new HashMap<>();
        playerCooldowns.put(user, null);

        triviaCount++;
        this.command = triviaCommand;
        questionDelayTimer = new Timer();

        /* Based on the tag, load all questions for this trivia game */
        boolean allTrivias = (tag.equalsIgnoreCase("all")) ? true : false;
        getTriviasMatchingTags(path, allTrivias);
        getTriviasMatchingTags(path + "custom/", allTrivias);
    }


    /**
     * Sets up the initial game of trivia. Checks if it is able to do so, then
     * adds this instance of trivia as an event listener. Then it generates
     * and sends the first question.
     */
    public void start() {

        channel.getJDA().addEventListener(this);

        if (Trivia.getTriviaCount() > MAX_TRIVIA_COUNT) {
            channel.sendMessage("There are currently too many ongoing " +
                            "trivia games being processed by this bot, perhaps in other servers" +
                            " as well. Please try again later.")
                    .queue();
            destroyInstance();
            return;
        }

        if (numTotalQuestions == 0) {
            channel.sendMessage("A trivia by this name or tag does not exist, " +
                    "or this server does not have permission to play this trivia. ").queue();
            destroyInstance();
            return;
        }

        sendNextQuestion();
    }


    /**
     * @return how many trivia games are currently active in the bot.
     */
    public static int getTriviaCount() {
        return triviaCount;
    }






    /**
     * @return a MessageEmbed containing the results of a trivia.
     */
    private MessageEmbed getResults() {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Results for the trivia!");

        /* Set description to list the chosen tag and all trivias included in this instance */
        StringBuilder descBuilder = new StringBuilder("Trivias played: ");
        for (String trivName : triviaNames) {
            descBuilder.append(trivName).append(",");
        }
        descBuilder.replace(descBuilder.length(), descBuilder.length(), "");
        descBuilder.append(" for tag: **" + tag + "**");
        String desc = descBuilder.toString();
        builder.setDescription(desc);

        /* Set fields: Players and their scores ranked from highest to lowest
         * must first sort players by their score from highest to lowest */
        List<Map.Entry<User,Long>> sortedScores = new ArrayList<Map.Entry<User, Long>>(
                playerToScore.entrySet()
        );
        Collections.sort(sortedScores, Collections.reverseOrder());

        int rank = 1;
        for (Map.Entry<User,Long> playerToScore : sortedScores) {
            User player = playerToScore.getKey();
            long score = playerToScore.getValue();

            builder.addField(
                    "**" + rank + ".** " + player.getName(),
                    Long.toString(score) + " point(s)",
                    false
            );
            rank+= 1;
        }

        builder.setColor(Color.BLUE);
        return builder.build();
    }


    /**
     * Handles an event where a user sends a message to this channel during
     * the game. Ignores messages from other channels or from bots.
     * Check if user is ending the game forcefully, then check to see if their
     * message matches a correct answer.
     *
     * @param event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String msg = event.getMessage().getContentRaw();
        User user = event.getAuthor();

        /* If message recieved in some other channel, ignore */
        if (!event.getChannel().equals(channel)) {
            return;
        }
        /* If message received is from a bot, ignore */
        if (user.isBot()) {
            return;
        }

        /* If player is in cooldown, ignore */
        if (playerCooldowns.containsKey(user) &&
                playerCooldowns.get(user) != null) {
            return;
        }



        /* Check for forceful end of trivia */
        if (msg.equalsIgnoreCase(Stoppable.CANCEL)
        || msg.equalsIgnoreCase(Stoppable.END)
        || msg.equalsIgnoreCase(Stoppable.STOP)) {
            stop(user, channel);
            return;
        }

        if (!readyToReceiveMessages) {
            return;
        }

        /* Check if correct answer */
        if (isCorrect(msg)) {
            addScoreTo(user);
            channel.sendMessage(getReplyUponCorrect(user)).queue();
            readyToReceiveMessages = false;
            questionTimer.cancel();
            visualCountdownTimer.cancel();
            questionDelayTimer.schedule(new NextQuestionTask(), 3000);
        }

        /*
         * If not, place an answer cooldown on the player if they
         * do not already have one.
         */
        else {
            if (!playerCooldowns.containsKey(user)) {
                playerCooldowns.put(user, null);
            }

            if (playerCooldowns.get(user) == null) {
                Consumer<Message> callback = (botMsg) -> {
                    Timer timer = new Timer();
                    timer.schedule(new RemovePlayerCooldownTask(user, botMsg), 4000);
                    playerCooldowns.put(user, timer);
                };
                channel.sendMessage("Wrong, " + user.getName() + "! Now wait " +
                                "4 seconds before you can answer again.")
                        .queue(callback);
            }


        }

    }



    /**
     * Randomizes what the next question will be
     */
    private void generateQuestionSeed() {
        Random random = new Random();
        currentQuestionIndex[0] = random.nextInt(triviaTypes.size());
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);
        currentQuestionIndex[1] = random.nextInt(type.getSize());
    }




    /**
     * @return The current trivia question
     */
    private String getQuestion() {
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);
        return type.getQuestionAt(currentQuestionIndex[1]);
    }

    /**
     * Generates and sends the next question to the channel
     * Sets up a timer for the question as a time limit
     */
    private void sendNextQuestion() {
        removeCurrentQuestion();

        generateQuestionSeed();
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);

        String fromMessage = "From trivia \"" + type.getName() + "\" made by " +
                type.getAuthor();

        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE);
        builder.setTitle("Question " + (numQuestionsAsked + 1) + ". (" +
                getPointsWorth() + " points)");
        builder.setFooter(Integer.toString(questionTimeLimit));

        builder.addField(
                fromMessage,
                getQuestion(),
                false
        );

        questionTimer = new Timer();
        questionTimer.schedule(new NextQuestionTask(), questionTimeLimit * 1000);

        /* Callback so we can reference the embed later to countdown the timer */
        Consumer<Message> callback = (msg) -> {
            questionMsg = msg;
            if (visualCountdownTimer != null) {
                visualCountdownTimer.cancel();
            }
            visualCountdownTimer = new Timer();
            visualCountdownTimer.scheduleAtFixedRate(
                    new TimerCountDownTask(questionTimeLimit),0, 1000);
        };
        channel.sendMessageEmbeds(builder.build()).queue(callback);

        /* Cancel any cooldowns users have for next question */
        for (User user : playerCooldowns.keySet()) {
            if (playerCooldowns.get(user) != null) {
                playerCooldowns.get(user).cancel();
                playerCooldowns.replace(user, null);
            }
        }

        numQuestionsAsked++;
        readyToReceiveMessages = true;
    }

    /**
     * Checks if the given answer by a user is correct.
     * It is correct if it matches any answer from the question's
     * answer list.
     *
     * @param userAns answer that a user input
     * @return true if user's answer is correct, false if not.
     */
    private boolean isCorrect(String userAns) {
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);
        List<String> answers = type.getAnswersAt(currentQuestionIndex[1]);

        for (String ans : answers) {
            if (userAns.equalsIgnoreCase(ans)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return how many points current question is worth.
     */
    private long getPointsWorth() {
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);
        long pointsWorth = type.getPointsAt(currentQuestionIndex[1]);
        return pointsWorth;
    }


    /**
     * Increments a player's score by the amount that the current question is
     * worth.
     * @param user player
     */
    private void addScoreTo(User user) {
        if (!playerToScore.containsKey(user)) {
            playerToScore.put(user, new Long(0));
        }

        long userNewScore = playerToScore.get(user) + getPointsWorth();

        playerToScore.replace(user, userNewScore);
    }


    /** Removes current question to prevent duplicates. If the trivia type removed from
     *  is now empty, remove it from the triviatype list. */
    private void removeCurrentQuestion() {

        /* Do nothing if current question has not been generated yet e.g. its the
         * first question of the trivia
         */
        if (currentQuestionIndex[0] == -1 || currentQuestionIndex[1] == -1) {
            return;
        }

        triviaTypes.get(currentQuestionIndex[0]).removeQuestion(currentQuestionIndex[1]);
        if (triviaTypes.get(currentQuestionIndex[0]).getSize() == 0) {
            triviaTypes.remove(currentQuestionIndex[0]);
        }
    }



    /**
     * Generates a string to reply with when a player gets an answer correct
     * @param user player who got the answer correct
     * @return reply string upon correct answer
     */
    private String getReplyUponCorrect(User user) {
        Random random = new Random();
        int seed = random.nextInt(25);

        String userName = user.getName();
        String triviaName = triviaTypes.get(currentQuestionIndex[0]).getName();

        switch (seed) {
            case 0:
                return "Correct! **+" + getPointsWorth() + "** to you!";
            case 1:
                return "You're on fire " + userName + "! **+" + getPointsWorth()
                        +  "** to you!";
            case 2:
                return "You're an expert at " + triviaName + ", aren't you?" +
                        " **+" + getPointsWorth() + "** to you!";
            case 3:
                return "Amazing job! **+" + getPointsWorth() + "** to " + userName + "!";
            case 4:
                return "Wow, look at " + userName + " getting **" + getPointsWorth() + "**" +
                        " points!";
            case 5:
                return userName + " did some studying. **+" + getPointsWorth() + "**!";
            case 6:
                return "**$" + getPointsWorth() + "** for you! Just kidding. " +
                        "**+" + getPointsWorth() + "** POINTS for you! ";
            case 7:
                return "LETS GOOOOO **" + getPointsWorth() + "** POINTS FOR " + userName + "!!!";
            case 8:
                return "Your knowledge of " + triviaName + " is like Airmilez knowledge of" +
                        " airplanes! **+" + getPointsWorth() + "**!";
            case 9:
                return "Yeah yeah, here are your " + getPointsWorth() + "points.";
            case 10:
                return "I can't believe someone got that right! **+" + getPointsWorth() + "**!";
            case 11:
                return "**+" + getPointsWorth() + "** to " + userName + "!";
            case 12:
                return "**+" + getPointsWorth() + "**! I think " + userName + " is gonna" +
                        " win this y'all.";
            case 13:
                return "Your knowledge of " + triviaName + " is like Becca knowledge of" +
                        " food! **+" + getPointsWorth() + "**!";
            case 14:
                return "Only **+" + getPointsWorth() + "** points? Amateur.";
            case 15:
                return "**+" + getPointsWorth() + "**! Maybe " + triviaName + " trivia too easy?";
            case 16:
                return "FINALLY someone got that correct. **+" + getPointsWorth() + "** to ya.";
            case 17:
                return "Oops, almost forgot to give " + userName + "those " + getPointsWorth() +
                        " points.";
            case 18:
                return "¡Aquí tienes " + getPointsWorth() + "puntos!";
            case 19:
                return "私がこっそり日本語を勉強したことを知っていましたか？ **+" + getPointsWorth() + "**!";
            case 20:
                return "Look at " + userName + " going BigBrainBecca mode! " +
                        "**+" + getPointsWorth() + "**!";
            case 21:
                return "Looks like ~~Kevin in a Silver Sedan~~ " + userName + " just earned **+" +
                        getPointsWorth() + "**!";
            case 22:
                return "**+" + getPointsWorth() + "** to you! Now go for 1,000 more.";
            case 23:
                return "Hey, stop cheating! Ah well, here is **+" + getPointsWorth() + "**!";
            case 24:
                return "Nice! Here is **+100000 points!** .. oops, its only **+" +
                        getPointsWorth() + "**!";
            default:
                return "You're amazing, " + userName + "! " +
                        "heres " + getPointsWorth() + " point for you!";
        }
    }



    /**
     * Determines if this trivia game instance is over. It is over if:
     * - A player hits the winning score
     * - All available questions were asked
     * - Max number of allowed questions were asked
     * - Game is forcefully stopped by a user, but this case is handled by the
     *   event listener.
     *
     * @return true if game is over, false if not.
     */
    private boolean isOver() {
        /* determine if any player obtained winning score */
        for (long score : playerToScore.values()) {
            if (score == winningScore) {
                return true;
            }
        }

        /* Determine if all available questions were exhausted */
        if (numQuestionsAsked >= numTotalQuestions) {
            return true;
        }

        /* Determine if maximum allowed questions were asked */
        if (numQuestionsAsked >= maxQuestions) {
            return true;
        }

        return false;
    }


    /**
     * Stops the game of trivia. Performs clean up operations and
     * sends the results to the channel this trivia takes place in.
     * Cancels any future timers that were awaiting execution.
     *
     * @param user user that triggered this event to end
     * @param channel where the event being ended is taking place
     */
    @Override
    public void stop(User user, MessageChannel channel) {
        channel.sendMessage("Trivia is over! Here are the results: ").queue();
        channel.sendMessageEmbeds(getResults()).queue();
        if (questionDelayTimer != null) {
            questionDelayTimer.cancel();
        }
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        if (visualCountdownTimer != null) {
            visualCountdownTimer.cancel();
        }

        destroyInstance();
    }



    /**
     * Performs clean-up operations of this trivia instance after it is
     * no-longer needed. Removes this instance from total active triviaCounts,
     * and removes this as a JDA event listener.
     */
    private void destroyInstance() {
        triviaCount--;
        channel.getJDA().removeEventListener(this);
        command.removeChannelFromActive(channelId);
    }


    /**
     * load appropriate trivias if they contain a matching tag or name
     * Loop through all files in trivia directory to see if the user-chosen tag
     * matches the trivia's tag or name. If so, add it to the trivia type list
     * for this trivia instance if the server is allowed to view it.
     *
     * @param path path to the trivias json files
     * @param allTrivias true if using the "all trivias" wildcard to load every trivia in server
     * */
    private void getTriviasMatchingTags(String path, boolean allTrivias) {
        final FileNameExtensionFilter extensionFilter =
                new FileNameExtensionFilter("N/A", "json");
        File tDir = new File(path);
        for (File file : tDir.listFiles()) {
            if (extensionFilter.accept(file) && file.isFile()) {
                String fileName = file.getName();
                TriviaType type = new TriviaType(path + fileName, channel.getJDA());
                List<String> tags = type.getTags();
                String trivName = type.getName();
                List<String> ids = type.getServers();
                boolean universal = type.isUniversal();
                boolean addTrivia = true;

                /* If all trivia wildcard is disabled & tags or name don't match current trivia,
                 * don't add it.
                 */
                if (!allTrivias) {
                    if (!tags.stream().anyMatch(tag::equalsIgnoreCase)
                            && !tag.equalsIgnoreCase(trivName)) {
                        addTrivia = false;
                    }
                }

                /* If trivia is not universal and trivia is not allowed in this server, don't add */
                if (!universal) {
                    if (ids.isEmpty() || !ids.contains(((TextChannel)channel).getGuild().getId())) {
                        addTrivia = false;
                    }
                }

                if (addTrivia) {
                    numTotalQuestions += type.getSize();
                    if (type.getSize() > 0) {
                        triviaTypes.add(type);
                    }
                    triviaNames.add(type.getName());
                }

            }
        }
    }


    /**
     * Returns whether a trivia exists
     * @param name name of the trivia
     * @param customOnly true if only to consider custom trivias
     * @return true if trivia exists, false if not
     */
    public static boolean triviaExists(String name, boolean customOnly) {

        List<String> customFileNames = IO.getAllFileNamesIn(path + "custom/");

        if (!customOnly) {
            List<String> defaultFileNames = IO.getAllFileNamesIn(path);
            for (String fileName : defaultFileNames) {
                if (IO.removeExtensionFromName(fileName).equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }

        for (String fileName : customFileNames) {
            if (IO.removeExtensionFromName(fileName).equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }



    /**
     * A TimerTask class that sends a new question.
     */
    private class NextQuestionTask extends TimerTask {

        public void run() {
            //visualCountdownTimer.cancel();

            /* Check if game over */
            if (isOver()) {
                stop(null, channel);
                return;
            }
            sendNextQuestion();
        }

    }


    /**
     * A TimerTask class that acts as a visual countdown on
     * a question MessageEmbed.
     */
    private class TimerCountDownTask extends TimerTask{

        /* Time left in seconds */
        private long timeLeftSeconds;

        public TimerCountDownTask(long timeLimit) {
            timeLeftSeconds = timeLimit;
        }

        public void run() {
            if (timeLeftSeconds > 0) {
                timeLeftSeconds--;
                MessageEmbed embed = questionMsg.getEmbeds().get(0);
                EmbedBuilder updatedEmbed = new EmbedBuilder();
                updatedEmbed.copyFrom(embed);
                updatedEmbed.setFooter(Long.toString(timeLeftSeconds));
                List<MessageEmbed> embeds = new ArrayList<>();
                embeds.add(updatedEmbed.build());

                questionMsg.editMessageEmbeds(embeds).queue();
            }
        }

    }


    /**
     * A TimerTask that removes a player's answer cooldown after
     * a certain amount of time has past.
     */
    private class RemovePlayerCooldownTask extends TimerTask {
        private User user;
        private Message msg;

        public RemovePlayerCooldownTask(User cooldownUser, Message botMsg) {
            user = cooldownUser;
            this.msg = botMsg;
        }

        public void run() {
            playerCooldowns.replace(user, null);
            msg.editMessage(user.getName() + "'s cooldown is over!")
                    .queue();
        }
    }


}
