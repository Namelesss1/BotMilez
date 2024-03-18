package commands.trivia.triviaeditor;

import commands.Stoppable;
import commands.trivia.QA;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.EmbedPageBuilder;
import util.IO;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.*;

import static util.EmbedPageBuilder.*;

/**
 * Represents an instance of a session where a user is modifying a
 * custom trivia.
 *
 */
public class TriviaEditSession extends ListenerAdapter implements Stoppable {

    protected String modifyScrollId;

    protected String scrollQuestionId;
    protected String createScrollId;
    protected String scrollUpdatedQuestionId;

    Map<String, EmbedPageBuilder> idToPageBuilder;

    /* Specifies the potential ways user interacts with the trivia editor */
    enum Action {
        UNDEFINED, /* User has not-yet selected one of the below */
        CREATE, /* Creating a new trivia */
        MODIFY, /* Modifying a specific element of an existing trivia */
        DELETE, /* Deleting a trivia */
    }

    /*
     * If a user is modifying an existing trivia, specifies the actions
     * being taken
     */
    protected enum ModifyAction {
        NONE,
        SELECT_ADD_OR_REMOVE,
        ADD, /* Adding a new element to the trivia */
        REMOVE /* Removing an element from the trivia */
    }

    /* For state machine : what the user is currently inputting.
    * This enum is organized in the order of the states that a user enters
    * when creating a new trivia. From START -> FINISHED */
    protected enum InputType {

        SELECT_TRIVIA, /* Selecting what trivia to modify */
        SELECT_ELEMENT, /* Selecting what to modify e.g. tags, questions, name */

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

   protected String startStr = "Type one of the below options to either create a new " +
           "trivia, modify an existing one, or delete a trivia entirely.\n" +
           "Remember, to end this process at anytime you can type 'stop' \n\n" +
           "```create (to make a new trivia)``` " +
           "```modify (to edit an existing trivia)``` " +
           "```delete (to delete an existing trivia entirely)``` ";

   public TriviaEditSession(User user) {
       this.user = user;
       inputType = InputType.START;
       action = Action.UNDEFINED;
       confirmState = ConfirmState.NORMAL;
       idToPageBuilder = new HashMap<>();

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


    /**
     * Gets a list of the names of trivias that a user has access to edit.
     * @param username username of user requesting edit access
     * @return a list of strings representing each trivia name that a user can access
     */
    public static Set<String> getAllowedTriviasForUser(String username) {

        Set<String> triviaNames = new HashSet<>();

        /* Load appropriate trivias into triviaTypes if they contain a matching tag
         * Loop through all files in trivia directory to see if the user-chosen tag
         * matches the trivia's tag or name. If so, add it to the trivia type list
         * for this trivia instance. */
        final FileNameExtensionFilter extensionFilter =
                new FileNameExtensionFilter("N/A", "json");
        File tDir = new File(path);
        for (File file : tDir.listFiles()) {
            if (extensionFilter.accept(file) && file.isFile()) {
                String fileName = file.getName();
                JSONObject trivObj = (JSONObject) IO.readJson(path + fileName);
                List<String> editors = (JSONArray)trivObj.get("allowed_editors");
                String trivName = (String)trivObj.get("name");
                String trivAuthor = (String)trivObj.get("author");
                if (editors.stream().anyMatch(username::equalsIgnoreCase)
                        || username.equalsIgnoreCase(trivAuthor)) {
                    triviaNames.add(trivName);
                }
            }
        }

        tDir = new File(path + "custom/");
        for (File file : tDir.listFiles()) {
            if (extensionFilter.accept(file) && file.isFile()) {
                String fileName = file.getName();
                JSONObject trivObj = (JSONObject)IO.readJson(path + "custom/" + fileName);
                String trivAuthor = (String)trivObj.get("author");
                List<String> editors = (JSONArray)trivObj.get("allowed_editors");
                String trivName = (String)trivObj.get("name");
                if (editors.stream().anyMatch(username::equalsIgnoreCase)
                        || username.equalsIgnoreCase(trivAuthor)) {
                   triviaNames.add(trivName);
                }
            }
        }

        return triviaNames;
    }


    /**
     * Takes a string list, and removes the duplicate string from it.
     * Even if two strings have different cases, they can still be duplicates
     * in this method. Which is why simply converting it to a set will not do
     * as it will retain string if it is in different cases.
     * @param list
     * @return
     */
    public static List<String> removeDuplicateStringsFromList(List<String> list) {
        Set<Integer> indexesToRemoveFrom = new HashSet<>();
        List<String> noDuplicatesList = new ArrayList<>(list);

        for (int index = 0; index < (list.size() - 1); index++) {
            for (int compareIndex = index + 1; compareIndex < list.size(); compareIndex++) {
                if (list.get(index).equalsIgnoreCase(list.get(compareIndex))) {
                    indexesToRemoveFrom.add(compareIndex);
                }
            }
        }

        for (Integer i : indexesToRemoveFrom) {
            noDuplicatesList.remove(list.get(i));
        }

        return noDuplicatesList;
    }


    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals(BUTTON_NEXT_PAGE + createScrollId) ||
                event.getComponentId().equals(BUTTON_PREVIOUS_PAGE + createScrollId) ||
                event.getComponentId().equals(DELETE_EMBED + createScrollId)) {
            idToPageBuilder.get(createScrollId).scroll(event);
        } else if (event.getComponentId().equals(BUTTON_NEXT_PAGE + modifyScrollId) ||
                event.getComponentId().equals(BUTTON_PREVIOUS_PAGE + modifyScrollId) ||
                event.getComponentId().equals(DELETE_EMBED + modifyScrollId)) {
            idToPageBuilder.get(modifyScrollId).scroll(event);
        } else if (event.getComponentId().equals(BUTTON_NEXT_PAGE + scrollQuestionId) ||
                event.getComponentId().equals(BUTTON_PREVIOUS_PAGE + scrollQuestionId) ||
                event.getComponentId().equals(DELETE_EMBED + scrollQuestionId)) {
            idToPageBuilder.get(scrollQuestionId).scroll(event);
        }
    }

}
