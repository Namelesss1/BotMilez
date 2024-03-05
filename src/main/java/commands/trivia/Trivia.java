package commands.trivia;

import commands.Stoppable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;

import java.util.List;

/**
 * This class represents an instance of a currently-ongoing game of
 * trivia that was activated through the TriviaCommand.
 *
 * Makes use of json files to retrieve questions, answers, and metadata
 * related to these such as how many points a question is worth.
 * A trivia file contains the following info:
 * name (used as id)
 * tags (list of strings to identify the trivia and to help group multiple ones)
 * is_default (boolean to differentiate if this is a custom trivia made by someone, or default)
 * trivia_author (creator of the trivia question came from)
 * qas (pairs of questions, corresponding answers, corresponding points worth)
 */
public class Trivia extends ListenerAdapter implements Stoppable {

    /* Amount of currently active trivias */
    private static int triviaCount = 0;

    public Trivia() {

    }

    public String getQuestion() {

    }

    @Override
    public void stop(User user, MessageChannel channel) {
        // decrement trivia count
        // send results embed
        // any general cleanup
        // Send message: "Trivia ended!"
    }


}
