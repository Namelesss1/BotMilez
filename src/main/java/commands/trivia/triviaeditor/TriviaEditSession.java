package commands.trivia.triviaeditor;

import commands.Stoppable;
import commands.trivia.QA;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an instance of a session where a user is modifying a
 * custom trivia.
 *
 */
public class TriviaEditSession extends ListenerAdapter implements Stoppable {

    /* Specifies the potential ways user interacts with the trivia editor */
    enum Action {

        UNDEFINED, /* User has not-yet selected one of the below */
        CREATE, /* Creating a new trivia */
        MODIFY, /* Modifying a specific element of an existing trivia */
        DELETE /* Deleting a trivia */
    }

    /*
     * If a user is modifying an existing trivia, specifies the actions
     * being taken
     */
    protected enum ModifyAction {
        ADD, /* Adding a new element to the trivia */
        REMOVE /* Removing an element from the trivia */
    }

    /* For state machine : what the user is currently inputting.
    * This enum is organized in the order of the states that a user enters
    * when creating a new trivia. From START -> FINISHED */
    protected enum InputType {

        SELECT_TRIVIA, /* Selecting what trivia to modify */
        SELECT_ELEMENT, /* Selecting what to modify e.g. tags, questions, name */
        SELECT_ADD_OR_REMOVE, /* Selecting whether to add or remove an element */

        START, /* Just beginning */
        NAME, /* Inputting name */
        TAGS, /* Inputting tags */
        UNIVERSAL, /* Inputting whether trivia is universally viewable */
        SERVERS, /* Input allowed servers that can view trivia */
        EDITORS, /* Input allowed contributors to trivia */
        QUESTION, /* Input a question */
        ANSWERS, /* Input answers to a question */
        POINTS, /* Input amount of points a quesion is worth */
        FINISHED, /* User has completed their editing action */

    }

    /* Specifies whether the user is confirming a choice they have made */
    protected enum ConfirmState {
        NORMAL,
        CONFIRM /* User is confirming an input choice they have made */
    }

    /* User that is currently editing/creating this trivia */
   protected User user;

   protected PrivateChannel channel;

   /* Channel for DM with user */
   protected String channelId;


   protected InputType inputType;

  protected Action action;

  protected ModifyAction modifyAction;

  protected ConfirmState confirmState;

   /* TriviaType object containing the modified information to write back */
   protected TriviaType triviaType;

   /* Question object containing current question information to write back */
   protected QA questionObj;

    protected static final String path = "resources/trivia/";

    private TriviaCreator creator;

    private TriviaModifier modifier;

   protected String startStr = "Type one of the below options to either create a new" +
           "trivia, modify an existing one, or delete a trivia entirely.\n" +
           "Remember, to end this process at anytime you can type 'stop' \n\n" +
           "```create``` ```modify``` ```delete```";

   public TriviaEditSession(User user) {
       this.user = user;
       inputType = InputType.START;
       action = Action.UNDEFINED;
       confirmState = ConfirmState.NORMAL;

       user.openPrivateChannel().flatMap(
               privChannel -> {
                   channelId = privChannel.getId();
                   channel = privChannel;
                   TriviaEditorCommand.addToActiveUser(user.getId());
                   return privChannel.sendMessage(startStr);
               }
       ).queue();

       user.getJDA().addEventListener(this);
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

        if (msg.equalsIgnoreCase(Stoppable.STOP) ||
        msg.equalsIgnoreCase(Stoppable.CANCEL) ||
        msg.equalsIgnoreCase(Stoppable.END)) {
            stop(user, channel);
            return;
        }

        if (action == Action.UNDEFINED) {
            if (msg.equalsIgnoreCase("create")) {
                action = Action.CREATE;
                creator = new TriviaCreator(this);
            }
            else if (msg.equalsIgnoreCase("modify")) {
                action = Action.MODIFY;
                inputType = inputType.SELECT_TRIVIA;
                modifier = new TriviaModifier(this);
            }
            else if (msg.equalsIgnoreCase("delete")) {
                action = Action.DELETE;
            }
            else {
                channel.sendMessage("You did not select an appropriate option").queue();
                return;
            }
        }

        else if (action == Action.CREATE) {
            creator.handleInput(msg);
        }

        else if (action == Action.MODIFY) {
            modifier.handleInput(msg);
        }

    }


    @Override
    public void stop(User user, MessageChannel channel) {
        channel.sendMessage("Stopping the trivia edit process...").queue();
        destroySession();
    }

    public void destroySession() {
        user.getJDA().removeEventListener(this);
        TriviaEditorCommand.removeActiveUsers(user.getId());
    }


    /**
     * Helper to extract tokens from a comma-separated string
     * @param str a string containing a list of separated elements e.g. "word1, ha ha, word3"
     * @return A list of strings, where each element is a phrase that was seperated by a comma.
     */
    public static List<String> parseCommaSeparatedList(String str) {
        if (!str.contains(",")) {
            ArrayList<String> list = new ArrayList<>();
            list.add(str);
            return list;
        }
        return Arrays.asList(str.split(",\\s*"));
    }





}
