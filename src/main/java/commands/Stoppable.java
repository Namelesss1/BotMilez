package commands;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

/**
 * Represents an action in the bot that can be forcefully
 * ended by a user through a keyword.
 */
public interface Stoppable {

    /* Keywords that will trigger an action in a class that implements this */
    public static final String STOP = "stop";
    public static final String CANCEL = "cancel";
    public static final String END = "end";

    /**
     * Ends some process once triggered.
     *
     * @param user user that triggered this event to end
     * @param channel where the event being ended is taking place
     */
    public void stop(User user, MessageChannel channel);

}
