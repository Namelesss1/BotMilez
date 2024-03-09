package commands.trivia.triviaeditor;

import commands.Stoppable;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
   private String channelId;

   private State prevState;
   private State nextState;

   private TriviaType triviaType;

   private String startStr = "Type one of the below options to either create a new" +
           "trivia, modify an existing one, or delete a trivia entirely.\n" +
           "Remember, to end this process at anytime you can type 'stop' \n\n" +
           "```create``` | ```modify``` | ```delete```";

   public TriviaEditSession(User user) {
       this.user = user;
       prevState = State.START;

       user.openPrivateChannel().flatMap(
               privChannel -> {
                   channelId = privChannel.getId();
                   return privChannel.sendMessage(startStr);
               }
       ).queue();

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


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        /* If bot, ignore */
        if (event.getAuthor().isBot()) {
            return;
        }

        /* If not the correct user, ignore */
        if (!event.getChannel().getId().equals(channelId)) {
            return;
        }
        if (!event.getAuthor().equals(user)) {
            return;
        }

        String msg = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

        if (prevState == State.START) {
            if (msg.equalsIgnoreCase("create")) {
                triviaType = new TriviaType(user.getName());
                channel.sendMessage("What would you like to name this new trivia?").queue();
                prevState = State.NAME;
                nextState = State.TAGS;
            }
            else if (msg.equalsIgnoreCase("modify")) {

            }
            else if (msg.equalsIgnoreCase("delete")) {

            }
            else {
                channel.sendMessage("You did not select an appropriate option").queue();
            }
        }

        else if (prevState == State.NAME) {

            String tagStr = "Enter any tags you want for this trivia. These tags help identify" +
                    "what the trivia is about. Seperate each one with a comma. For example," +
                    "if I make a trivia for Super Mario Bros., I might type this:\n\n" +
                    "super mario bros, mario, nintendo, nes, fun game, mario bros";

            channel.sendMessage(tagStr).queue();
        }

    }


}
