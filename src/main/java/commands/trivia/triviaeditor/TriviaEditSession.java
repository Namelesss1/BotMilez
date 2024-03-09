package commands.trivia.triviaeditor;

import commands.IBotCommand;
import commands.Stoppable;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TriviaEditSession extends ListenerAdapter implements Stoppable {

    /* For state machine : what the user is currently inputting */
    private enum State {
        START,
        NAME,
        TAGS,
        UNIVERSAL,
        SERVERS,
        EDITORS,
        QUESTION,
        ANSWERS,
        POINTS
    }

    /* User that is currently editing/creating this trivia */
   private User user;
   /* Channel for DM with user */
   private MessageChannel channel;

   private State currState;
   private State nextState;

   public TriviaEditSession(User user) {
       this.user = user;
       currState = State.START;
       

       user.getJDA().addEventListener(this);
   }

   @Override
   public void stop(User user, MessageChannel channel) {
       destroySession();
   }

   public void destroySession() {
       user.getJDA().removeEventListener(this);
   }

    /**
     * Sends a Private Message to a user.
     * @param msg message content
     */
    private void sendPM(String msg) {
        user.openPrivateChannel().complete()
                .sendMessage(msg).queue();
    }

}
