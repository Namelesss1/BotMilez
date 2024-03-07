package commands.trivia;

import commands.Stoppable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.IO;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.*;

/**
 * This class represents an instance of a currently-ongoing game of
 * trivia that was activated through the TriviaCommand.
 *
 * TODO: Include a wide range of different ways to say "correct!"
 * TODO: Implement stop() functionality
 * TODO: Implement question timer
 * TODO: Small pause between each question
 */
public class Trivia extends ListenerAdapter implements Stoppable {

    /* Maximum allowed trivias being played on this bot at once */
    private static final int MAX_TRIVIA_COUNT = 10;

    /* Amount of currently active trivias */
    private static int triviaCount = 0;

    /* List of trivia types that are used in this trivia instance */
    private List<TriviaType> triviaTypes;

    /* Maps of active players -> score in this trivia */
    private Map<User, Long> playerToScore;

    /* Maximum number of questions to ask before ending trivia */
    private int maxQuestions;

    /* Max amount of points a player can score to win trivia */
    private long winningScore;

    /* Time limit for each question (in seconds) before moving on to next */
    private int questionTimeLimit;

    /* Message Channel of which this trivia is happening in */
    MessageChannel channel;

    /* Path to the trivias */
    private final String path = "resources/trivia/";

    /* Total number of questions in this trivia */
    private int numTotalQuestions;

    /* Number of questions asked thus far */
    private int numQuestionsAsked;


    /*
     * An array of size two that helps keep track of which question is
     * currently active.
     * index [0]: index of which trivia type in triviaTypes list
     * index [1]: id of question within the trivia type
     */
    private int[] currentQuestionIndex;


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
    public Trivia(String tag, int maxQ, int maxPoints, int timeLimit, MessageChannel channel, User user) {
        maxQuestions = maxQ;
        winningScore = maxPoints;
        questionTimeLimit = timeLimit;
        this.channel = channel;
        triviaTypes = new ArrayList<>();
        currentQuestionIndex = new int[2];
        numTotalQuestions = 0;
        numQuestionsAsked = 0;
        playerToScore = new HashMap<>();
        playerToScore.put(user, new Long(0));
        triviaCount++;

        /* Load appropriate trivias into triviaTypes if they contain a matching tag
         * Loop through all files in trivia directory to see if the user-chosen tag
         * matches the trivia's tag or name. If so, add it to the trivia type list
         * for this trivia instance. */
        //TODO: Modify behavior to account for custom trivias (only those in this server)
        final FileNameExtensionFilter extensionFilter =
                new FileNameExtensionFilter("N/A", "json");
        File tDir = new File(path);
        for (File file : tDir.listFiles()) {
            if (extensionFilter.accept(file) && file.isFile()) {
                String fileName = file.getName();
                JSONObject trivObj = (JSONObject)IO.readJson(path + fileName);
                List<String> tags = (JSONArray)trivObj.get("tags");
                String trivName = (String)trivObj.get("name");
                if (tags.contains(tag.toLowerCase()) || tag.equalsIgnoreCase(trivName)) {
                    TriviaType type = new TriviaType(trivObj);
                    numTotalQuestions += type.getSize();
                    triviaTypes.add(type);
                }
            }
        }

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
            channel.sendMessage("A trivia by this name or tag does not exist.").queue();
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
     * Stops the game of trivia. Performs clean up operations and
     * sends the results to the channel this trivia takes place in.
     *
     * @param user user that triggered this event to end
     * @param channel where the event being ended is taking place
     */
    @Override
    public void stop(User user, MessageChannel channel) {
        channel.sendMessage("Trivia is over! Here are the results: ").queue();
        // send results embed
        destroyInstance();
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

        /* If message recieved in some other channel, ignore */
        if (!event.getChannel().equals(channel)) {
            return;
        }
        /* If message received is from a bot, ignore */
        if (event.getAuthor().isBot()) {
            return;
        }

        String msg = event.getMessage().getContentRaw();

        /* Check for forceful end of trivia */
        if (msg.equalsIgnoreCase(Stoppable.CANCEL)
        || msg.equalsIgnoreCase(Stoppable.END)
        || msg.equalsIgnoreCase(Stoppable.STOP)) {
            stop(event.getAuthor(), channel);
            return;
        }

        /* Check if correct answer */
        if (isCorrect(msg)) {
            addScoreTo(event.getAuthor());
            channel.sendMessage(getReplyUponCorrect(event.getAuthor())).queue();

            /* Check if game over */
            if (isOver()) {
                stop(event.getAuthor(), channel);
                return;
            }

            /* Remove this question to prevent duplicates. If the trivia type removed from
             *  is now empty, remove it from the triviatype list. */
            triviaTypes.get(currentQuestionIndex[0]).removeQuestion(currentQuestionIndex[1]);
            if (triviaTypes.get(currentQuestionIndex[0]).getSize() == 0) {
                triviaTypes.remove(currentQuestionIndex[0]);
            }

            /* Send next question */
            sendNextQuestion();
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
     * @return The current trivia question
     */
    private String getQuestion() {
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);
        return type.getQuestionAt(currentQuestionIndex[1]);
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


    /**
     * Generates and sends the next question to the channel
     */
    private void sendNextQuestion() {
        generateQuestionSeed();
        TriviaType type = triviaTypes.get(currentQuestionIndex[0]);
        //String defaultStr = type.isDefault() ? ("default question") : ("custom question");
        String message = "**Question " + (numQuestionsAsked + 1) + "** from trivia " +
                "** " + type.getName() + "** made by ** " + type.getAuthor() + "** " +
                "(points: " + getPointsWorth() + ") :";
        channel.sendMessage(message + "\n" + getQuestion()).queue();
        numQuestionsAsked++;
    }


    /**
     * Generates a string to reply with when a player gets an answer correct
     * @param user player who got the answer correct
     * @return reply string upon correct answer
     */
    private String getReplyUponCorrect(User user) {
        Random random = new Random();
        int seed = random.nextInt(5);

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
                        " **+" + getPointsWorth() + " to you!";
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
                return "**+" + getPointsWorth() + "**! I think " + userName + "is gonna" +
                        "win this y'all.";
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
     * Performs clean-up operations of this trivia instance after it is
     * no-longer needed. Removes this instance from total active triviaCounts,
     * and removes this as a JDA event listener.
     */
    private void destroyInstance() {
        triviaCount--;
        channel.getJDA().removeEventListener(this);
    }





}
