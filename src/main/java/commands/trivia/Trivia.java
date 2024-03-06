package commands.trivia;

import commands.Stoppable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
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
 */
public class Trivia extends ListenerAdapter implements Stoppable {

    /* Maximum allowed trivias being played on this bot at once */
    private static final int MAX_TRIVIA_COUNT = 10;

    /* Amount of currently active trivias */
    private static int triviaCount = 0;

    /* List of trivia types that are used in this trivia instance */
    private List<TriviaType> triviaTypes;

    /* Maps of active players -> score in this trivia */
    private Map<User, Integer> playerToScore;

    /* Maximum number of questions to ask before ending trivia */
    private int maxQuestions;

    /* Max amount of points a player can score to win trivia */
    private int winningScore;

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
        playerToScore.put(user, 0);
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



    }

    public static int getTriviaCount() {
        return triviaCount;
    }

    @Override
    public void stop(User user, MessageChannel channel) {
        // send results embed
        // Send message: "Trivia ended!"


        destroyInstance();
    }

    /**
     * Randomizes what the next question will be
     */
    private void generateQuestionSeed() {
        Random random = new Random();

    }

    /**
     * Determines if this trivia game instance is over. It is over if:
     * - A player hits the winning score
     * - All available questions were asked, or total number allowed questions were asked
     * - Game is forcefully stopped by a user
     *
     * @return true if game is over, false if not.
     */
    private boolean isOver() {
        
    }

    private String getQuestion() {

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
