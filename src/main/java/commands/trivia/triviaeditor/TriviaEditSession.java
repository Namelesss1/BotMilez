package commands.trivia.triviaeditor;

import com.sun.org.apache.xpath.internal.operations.Mod;
import commands.Stoppable;
import commands.trivia.QA;
import commands.trivia.Trivia;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.JSONObject;
import util.IO;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a session where a user is modifying a
 * custom trivia.
 *
 * //TODO: When editing question, check if name is available + further checks
 * //TODO: Verify both user and bot are in mentioned server
 */
public class TriviaEditSession extends ListenerAdapter implements Stoppable {

    /* Specifies the potential ways user interacts with the trivia editor */
    private enum Action {

        UNDEFINED, /* User has not-yet selected one of the below */
        CREATE, /* Creating a new trivia */
        MODIFY, /* Modifying a specific element of an existing trivia */
        DELETE /* Deleting a trivia */
    }

    /*
     * If a user is modifying an existing trivia, specifies the actions
     * being taken
     */
    private enum ModifyAction {

        SELECT_TRIVIA, /* Selecting what trivia to modify */
        SELECT_ELEMENT, /* Selecting what to modify e.g. tags, questions, name */
        SELECT_ADD_OR_REMOVE, /* Selecting whether to add or remove an element */
        ADD, /* Adding a new element to the trivia */
        REMOVE /* Removing an element from the trivia */
    }

    /* For state machine : what the user is currently inputting.
    * This enum is organized in the order of the states that a user enters
    * when creating a new trivia. From START -> FINISHED */
    private enum InputType {
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

    /* Specifies whether the user is fixing a mistake */
    private enum CorrectState {
        CORRECT, /* User has not made a mistake in input */
        MISTAKE /* User has made a mistake in input, has to correct */
    }

    /* Specifies whether the user is confirming a choice they have made */
    private enum ConfirmState {
        NORMAL,
        CONFIRM /* User is confirming an input choice they have made */
    }

    /* User that is currently editing/creating this trivia */
   private User user;

   private MessageChannel channel;

   /* Channel for DM with user */
   private String channelId;


   private InputType inputType;

  private Action action;

  private ModifyAction modifyAction;

  private CorrectState correctState;

  private ConfirmState confirmState;

   /* TriviaType object containing the modified information to write back */
   private TriviaType triviaType;

   /* Question object containing current question information to write back */
   private QA questionObj;

    private static final String path = "resources/trivia/";

   private String startStr = "Type one of the below options to either create a new" +
           "trivia, modify an existing one, or delete a trivia entirely.\n" +
           "Remember, to end this process at anytime you can type 'stop' \n\n" +
           "```create``` | ```modify``` | ```delete```";

   public TriviaEditSession(User user) {
       this.user = user;
       inputType = InputType.START;
       action = Action.UNDEFINED;
       correctState = CorrectState.CORRECT;
       confirmState = ConfirmState.NORMAL;

       user.openPrivateChannel().flatMap(
               privChannel -> {
                   channelId = privChannel.getId();
                   channel = privChannel;
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

        if (action == Action.UNDEFINED) {
            if (msg.equalsIgnoreCase("create")) {
                action = Action.CREATE;
            }
            else if (msg.equalsIgnoreCase("modify")) {
                action = Action.MODIFY;
                modifyAction = ModifyAction.SELECT_TRIVIA;
            }
            else if (msg.equalsIgnoreCase("delete")) {
                action = Action.DELETE;
            }
            else {
                channel.sendMessage("You did not select an appropriate option").queue();
            }
        }


        if (inputType == InputType.START) {
            promptName();
        }
        else if (inputType == InputType.NAME) {
            processNameInput(msg);
        }
        else if (inputType == InputType.TAGS) {
            processTagsInput(msg);
        }
        else if (inputType == InputType.UNIVERSAL) {
            processUniversalInput(msg);
        }
        else if (inputType == InputType.SERVERS) {
            processServersInput(msg);
        }
        else if (inputType == InputType.EDITORS) {
            processEditorsInput(msg);
        }
        else if (inputType == InputType.QUESTION) {
            processQuestionInput(msg);
        }
        else if (inputType == InputType.ANSWERS) {
            processAnswerInput(msg);
        }
        else if (inputType == InputType.POINTS) {
            processPointsInput(msg);
        }
        else if (inputType == InputType.FINISHED) {
            processMoreQuestionPrompt(msg);

        }

    }


    /**
     * Handles queries when a user chooses to modify an existing trivia
     * @param event
     */
    private void handleModify(MessageReceivedEvent event) {

        String msg = event.getMessage().getContentRaw();




    }


    /**
     * Handles queries when a user chooses to delete an existing trivia
     * @param event
     */
    private void handleDelete(MessageReceivedEvent event) {

        String msg = event.getMessage().getContentRaw();
        MessageChannel channel = event.getChannel();

    }


    private void processStartInput(String input) {
        if (action == Action.CREATE) {
            triviaType = new TriviaType();
            triviaType.setAuthor(user.getName());
            inputType = InputType.NAME;
        }

        promptName();
    }


    /**
     * If a user selected to modify an existing trivia, ensures the trivia
     * exists and then prompts what element of the trivia the user wants to modify.
     * Sends an error to the user if trivia does not exist, or they don't have permissions
     * to modify it and prompts them to try again.
     *
     * @param name name of trivia to modify
     */
    private void processTriviaSelect(String name) {
        if (!Trivia.triviaExists(name)) {
            channel.sendMessage("The trivia, " + name + " does not exist!" +
                    " Please try inputting another name.")
                    .queue();
            return;
        }

        /* Load the trivia if found */
        triviaType = new TriviaType(path + "/custom" +name);
        modifyAction = ModifyAction.SELECT_ELEMENT;

        if (!triviaType.getEditors().contains(user.getName())) {
            channel.sendMessage("You do not have permission to edit this trivia." +
                    " Maybe you can request permission from the creator.").queue();
            stop(user, channel);
            return;
        }
    }


    /**
     *
     * @param element
     */
    private void processElementSelect(String element) {
        
    }

    /**
     * Sets the name of a trivia and verifies it is correct
     * A name is valid if it is not the same as an existing trivia.
     *
     * @param name name of trivia
     */
    private void processNameInput(String name) {
        if (Trivia.triviaExists(name)) {
            channel.sendMessage("A trivia named " + name + " already exists! " +
                            "try another name.")
                    .queue();

            correctState = CorrectState.MISTAKE;
            return;
        }

        if (correctState == correctState.CORRECT) {
            if (action == Action.CREATE) {
                channel.sendMessage("Your trivia name is: **" + name + "**").queue();

                triviaType.setName(name);
                String tagStr = "Enter any tags you want for this trivia. These tags help identify" +
                        "what the trivia is about. Seperate each one with a comma. For example," +
                        "if I make a trivia for Super Mario Bros., I might type this:\n\n" +
                        "super mario bros, mario, nintendo, nes, fun game, mario bros";

                channel.sendMessage(tagStr).queue();
                inputType = InputType.TAGS;
                correctState = CorrectState.CORRECT;
            }

            if (action == Action.MODIFY) {
                channel.sendMessage("Success: Your new trivia name is: **" + name + "**")
                        .queue();
                stop(user, channel);
            }
        }
    }


    /**
     * Processes user inputting tags for a trivia.
     * @param tagsStr tags to set
     */
    private void processTagsInput(String tagsStr) {
        if (action == Action.CREATE) {
            List<String> tags = TriviaEditor.parseCommaSeparatedList(tagsStr);
            channel.sendMessage("Your tags are: **" + tags.toString() + "**").queue();
            triviaType.setTags(tags);
            String universalStr = "Do you want this trivia to be viewable across all servers i'm in?" +
                    "Type yes or no.";

            channel.sendMessage(universalStr).queue();
            inputType = InputType.UNIVERSAL;
        }
    }


    /**
     * Processes a user's response as to whether or not to make their trivia
     * public in all servers the bot is in.
     * User's response is invalid if it is not "yes" or "no"
     *
     * @param universalStr string representing yes or no
     */
    private void processUniversalInput(String universalStr) {
        boolean universal;
        if (universalStr.equalsIgnoreCase("yes")) {
            channel.sendMessage("Your trivia will be viewable across all servers i'm in.").queue();
            universal = true;
        }
        else if (universalStr.equalsIgnoreCase("no")) {
            channel.sendMessage("Your trivia will be viewable in only the servers you choose.").queue();
            universal = false;
        }
        else {
            channel.sendMessage(universalStr + " is not recognized. Please type yes or no.").queue();
            return;
        }
        triviaType.setUniversal(universal);

        String str;
        if (universal) {
            str = "Enter the discord usernames of all people you want to be able to " +
                    "contribute to this trivia. These people will be able to edit, modify," +
                    "contribute to it, or delete it. Use a comma-separated list e.g.:\n\n" +
                    "bob41, mike10, awesomeUser";

            inputType = InputType.EDITORS;
        }
        else {
            str = "Enter the names all of the servers we're both in that you want this trivia to be" +
                    " playable in. Use a comma-separated list e.g. \n\n" +
                    "my test server, The Clubhouse, Jim's Hangout Server";

            inputType = InputType.SERVERS;
        }

        channel.sendMessage(str).queue();
    }


    /**
     * Processes the user's input as to which servers to allow.
     *
     * @param serverStr User response representing all server names they want to allow.
     */
    private void processServersInput(String serverStr) {
        List<String> servers = TriviaEditor.parseCommaSeparatedList(serverStr);
        List<Long> serverIds = new ArrayList<>();
        for (Guild server : user.getMutualGuilds()) {
            if (servers.contains(server.getName().toLowerCase())) {
                serverIds.add(server.getIdLong());
            }

        }
        triviaType.setServers(serverIds);

        List<String> serverNames = new ArrayList<>();
        for (Long serverId : serverIds) {
            serverNames.add(user.getJDA().getGuildById(serverId).getName());
        }
        channel.sendMessage("Your trivia will be viewable in the following servers: " +
                serverNames.toString()).queue();

        String str = "Enter the discord usernames of all people you want to be able to " +
                "contribute to this trivia. These people will be able to edit, modify," +
                "contribute to it, or delete it. Use a comma-separated list e.g.:\n\n" +
                "bob41, mike10, awesomeUser\n" +
                "If you wish to add no one else, just type none";
        inputType = InputType.EDITORS;
        channel.sendMessage(str).queue();
    }


    /**
     * Processes user's inputs of who they want to grant edit access to for the trivia.
     * @param editorStr string representing user's list of editors allowed
     */
    private void processEditorsInput(String editorStr) {
        List<String> editors = new ArrayList<>();
        editors.add(triviaType.getAuthor());
        if (!editorStr.trim().equalsIgnoreCase("none")) {
            List<String> additionalEditors = TriviaEditor.parseCommaSeparatedList(editorStr);
            editors.addAll(additionalEditors);
        }

        triviaType.setEditors(editors);
        channel.sendMessage("The following users will have permission to edit your trivia: " +
                editors.toString()).queue();


        String str = "Enter a question you would like to ask in this trivia. Or type" +
                "stop if you are finished";
        inputType = InputType.QUESTION;
        channel.sendMessage(str).queue();
    }


    /**
     * Processes questions that the user decides to input
     *
     * @param quesStr string representing question prompt user wants to ask
     */
    private void processQuestionInput(String quesStr) {
        if (correctState == CorrectState.CORRECT) {
            questionObj = new QA();
            questionObj.setId(triviaType.getNextQuestionId());
            questionObj.setQuestion(quesStr);
        }

        String str = "Now enter any correct answers to the question you asked. Use a " +
                "comma-separated list e.g.\n\n" +
                "mario kart wii, mkw, mkwii";
        channel.sendMessage(str).queue();
        inputType = InputType.ANSWERS;
        correctState = CorrectState.CORRECT;
    }


    /**
     * Processes answers that the user wants to set for a corresponding question prompt
     * @param ansStr string representing a list of answers the user wants
     */
    private void processAnswerInput(String ansStr) {
        if (correctState == CorrectState.CORRECT) {
            List<String> answers = TriviaEditor.parseCommaSeparatedList(ansStr);
            questionObj.setAnswer(answers);
        }

        String str = "Enter how many points this question is worth. " +
                "Allowed amount is between 1 and 3 points";
        inputType = InputType.POINTS;
        channel.sendMessage(str).queue();
    }


    /**
     * Processes the points that the user wants a corresponding question to be worth.
     * Checks that user's input is a number, and a number between the allowed range
     * that points can be.
     *
     * @param ptStr string representing number of points that the question is worth.
     */
    private void processPointsInput(String ptStr) {
        long pts;
        try {
            pts = Long.parseLong(ptStr.trim());
        }
        catch (NumberFormatException e) {
            channel.sendMessage("You did not enter a number. Please try again.").queue();
            correctState = CorrectState.MISTAKE;
            return;
        }

        if (pts < 0 || pts > 3) {
            channel.sendMessage("Your number is not in the inclusive range of 1-3." +
                    " Please try again.").queue();
            correctState = CorrectState.MISTAKE;
            return;
        }

        questionObj.setPoints(pts);
        triviaType.addQuestion(questionObj);

        channel.sendMessage("Here is your question:").queue();
        channel.sendMessageEmbeds(questionObj.asEmbed()).queue();

        String str = "Do you wish to input another question? Type yes or no";
        inputType = InputType.FINISHED;
        channel.sendMessage(str).queue();
        correctState = CorrectState.CORRECT;
    }



    /**
     * Processes a user's response to being asked if they want to input more questions to a trivia.
     * Answer must be "yes" or "no", otherwise error message sent to user so they can try again.
     *
     * @param response user's response to prompt whether they want to continue inputting more questions.
     */
    public void processMoreQuestionPrompt(String response) {
        if (response.trim().equalsIgnoreCase("yes")) {
            String str = "Enter a question you would like to ask in this trivia. Or type" +
                    "stop if you are finished";
            inputType = InputType.QUESTION;
            channel.sendMessage(str).queue();
        }
        else {
            //TODO: Write triviaType to file using TriviaEditor
            channel.sendMessage("Here is the trivia you created. " +
                    "You may edit it at any time if you want to modify something." +
                    " Thanks for making a trivia!").queue();
            channel.sendMessageEmbeds(triviaType.asEmbed()).queue();
            channel.sendMessageEmbeds(triviaType.asQuestionsEmbed()).queue();
            stop(user, channel);
        }
    }



    public void promptName() {
        if (action == Action.CREATE) {
            channel.sendMessage("What would you like to name this new trivia?").queue();
        }
        else if (action == Action.MODIFY) {
            if (modifyAction == ModifyAction.SELECT_TRIVIA) {
                channel.sendMessage("Enter name of the trivia you want to edit").queue();
            }
            else {
                channel.sendMessage("What would you like to rename this trivia to?").queue();
            }
        }
    }



    public void promptTags() {

    }

    public void promptUniversal() {

    }

    public void promptServers() {

    }

    public void promptEditors() {

    }

    public void promptQuestion() {

    }

    public void promptAnswers() {

    }

    public void promptPoints() {

    }

    public void promptMoreQuestions() {

    }




    @Override
    public void stop(User user, MessageChannel channel) {
        channel.sendMessage("Stopping the trivia edit process...").queue();
        destroySession();
    }

    public void destroySession() {
        user.getJDA().removeEventListener(this);
        //TODO: Close private channel
    }







}
