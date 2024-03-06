package commands.trivia;

import commands.Stoppable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

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


    public Trivia(String tag, int maxQ, int maxPoints, int timeLimit) {
        maxQuestions = maxQ;
        winningScore = maxPoints;
        questionTimeLimit = timeLimit;
        triviaCount++;

        
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

    private void destroyInstance() {
        triviaCount--;
        channel.getJDA().removeEventListener(this);
    }


}
