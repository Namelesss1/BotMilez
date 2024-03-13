package commands.trivia.triviaeditor;

import commands.trivia.QA;
import commands.trivia.Trivia;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.List;

public class TriviaCreator {

    private TriviaEditSession session;

    public TriviaCreator(TriviaEditSession session) {
        this.session = session;

        /* Create new triviaType to represent the trivia. Then prompt the
         * user for the name of the new trivia
         */
        session.triviaType = new TriviaType();
        session.triviaType.setAuthor(session.user.getName());
        session.inputType = TriviaEditSession.InputType.NAME;
        promptName();
    }

    public void handleInput(String input) {
        if (session.inputType == TriviaEditSession.InputType.START) {
            promptName();
        }
        else if (session.inputType == TriviaEditSession.InputType.NAME) {
            processNameInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.TAGS) {
            processTagsInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.UNIVERSAL) {
            processUniversalInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.SERVERS) {
            processServersInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.EDITORS) {
            processEditorsInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.QUESTION) {
            processQuestionInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.ANSWERS) {
            processAnswerInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.POINTS) {
            processPointsInput(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.FINISHED) {
            processMoreQuestionPrompt(input);
        }

    }


    /**
     * Sets the name of a trivia and verifies it is correct
     * A name is valid if it is not the same as an existing trivia.
     *
     * @param name name of trivia
     */
    private void processNameInput(String name) {
        if (Trivia.triviaExists(name)) {
            session.channel.sendMessage("A trivia named " + name + " already exists! " +
                            "try another name.")
                    .queue();
            return;
        }

        session.channel.sendMessage("Your trivia name is: **" + name + "**").queue();
        session.triviaType.setName(name);
        promptTags();
        session.inputType = TriviaEditSession.InputType.TAGS;

    }


    /**
     * Processes user inputting tags for a trivia.
     * @param tagsStr tags to set
     */
    private void processTagsInput(String tagsStr) {
        List<String> tags = TriviaEditSession.parseCommaSeparatedList(tagsStr);
        session.channel.sendMessage("Your tags are: **" + tags.toString() + "**").queue();
        session.triviaType.setTags(tags);
        promptUniversal();
        session.inputType = TriviaEditSession.InputType.UNIVERSAL;
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
            session.channel.sendMessage("Your trivia will be viewable " +
                    "across all servers i'm in.").queue();
            universal = true;
        }
        else if (universalStr.equalsIgnoreCase("no")) {
            session.channel.sendMessage("Your trivia will be viewable" +
                    " in only the servers you choose.").queue();
            universal = false;
        }
        else {
            session.channel.sendMessage(universalStr + " is not recognized." +
                    " Please type yes or no.").queue();
            return;
        }
        session.triviaType.setUniversal(universal);


        if (universal) {
            promptEditors();
            session.inputType = TriviaEditSession.InputType.EDITORS;
        }
        else {
           promptServers();
            session.inputType = TriviaEditSession.InputType.SERVERS;
        }
    }


    /**
     * Processes the user's input as to which servers to allow.
     *
     * @param serverStr User response representing all server names they want to allow.
     */
    private void processServersInput(String serverStr) {
        List<String> servers = TriviaEditSession.parseCommaSeparatedList(serverStr);
        List<Long> serverIds = new ArrayList<>();
        for (Guild server : session.user.getMutualGuilds()) {
            if (servers.contains(server.getName().toLowerCase())) {
                serverIds.add(server.getIdLong());
            }

        }
        session.triviaType.setServers(serverIds);

        List<String> serverNames = new ArrayList<>();
        for (Long serverId : serverIds) {
            serverNames.add(session.user.getJDA().getGuildById(serverId).getName());
        }
        session.channel.sendMessage("Your trivia will be viewable in the following servers: " +
                serverNames.toString()).queue();

        promptEditors();
        session.inputType = TriviaEditSession.InputType.EDITORS;
    }


    /**
     * Processes user's inputs of who they want to grant edit access to for the trivia.
     * @param editorStr string representing user's list of editors allowed
     */
    private void processEditorsInput(String editorStr) {
        List<String> editors = new ArrayList<>();
        editors.add(session.triviaType.getAuthor());
        if (!editorStr.trim().equalsIgnoreCase("none")) {
            List<String> additionalEditors = TriviaEditSession.parseCommaSeparatedList(editorStr);
            editors.addAll(additionalEditors);
        }

        session.triviaType.setEditors(editors);
        session.channel.sendMessage("The following users will have " +
                "permission to edit your trivia: " +
                editors.toString()).queue();


        promptQuestion();
        session.inputType = TriviaEditSession.InputType.QUESTION;
    }


    /**
     * Processes questions that the user decides to input
     *
     * @param quesStr string representing question prompt user wants to ask
     */
    private void processQuestionInput(String quesStr) {
        session.questionObj = new QA();
        session.questionObj.setId(session.triviaType.getNextQuestionId());
        session.questionObj.setQuestion(quesStr);

        promptAnswers();
        session.inputType = TriviaEditSession.InputType.ANSWERS;
    }


    /**
     * Processes answers that the user wants to set for a corresponding question prompt
     * @param ansStr string representing a list of answers the user wants
     */
    private void processAnswerInput(String ansStr) {
        List<String> answers = TriviaEditSession.parseCommaSeparatedList(ansStr);
        session.questionObj.setAnswer(answers);

        promptPoints();
        session.inputType = TriviaEditSession.InputType.POINTS;
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
            session.channel.sendMessage("You did not enter a number. Please try again.")
                    .queue();
            return;
        }

        if (pts < 0 || pts > 3) {
            session.channel.sendMessage("Your number is not in the inclusive range of 1-3." +
                    " Please try again.").queue();
            return;
        }

        session.questionObj.setPoints(pts);
        session.triviaType.addQuestion(session.questionObj);
        session.channel.sendMessage("Here is your question:").queue();
        session.channel.sendMessageEmbeds(session.questionObj.asEmbed()).queue();

        promptMoreQuestions();
        session.inputType = TriviaEditSession.InputType.FINISHED;
    }



    /**
     * Processes a user's response to being asked if they want to input more questions to a trivia.
     * Answer must be "yes" or "no", otherwise error message sent to user so they can try again.
     *
     * @param response user's response to prompt whether they want to continue inputting more questions.
     */
    public void processMoreQuestionPrompt(String response) {
        if (response.trim().equalsIgnoreCase("yes")) {
            promptQuestion();
            session.inputType = TriviaEditSession.InputType.QUESTION;
        }
        else {
            //TODO: Write triviaType to file using TriviaEditor
            session.channel.sendMessage("Here is the trivia you created. " +
                    "You may edit it at any time if you want to modify something." +
                    " Thanks for making a trivia!").queue();
            session.channel.sendMessageEmbeds(session.triviaType.asEmbed()).queue();
            session.channel.sendMessageEmbeds(session.triviaType.asQuestionsEmbed()).queue();
            session.stop(session.user, session.channel);
        }
    }




    public void promptName() {
            session.channel.sendMessage("What would you like to name this new trivia?")
                    .queue();
    }



    public void promptTags() {
        session.channel.sendMessage("Enter any tags you want for this trivia. " +
                                "These tags help identify" +
                "what the trivia is about. Seperate each one with a comma. For example," +
                "if I make a trivia for Super Mario Bros., I might type this:\n\n" +
                "super mario bros, mario, nintendo, nes, fun game, mario bros")
                .queue();
    }

    public void promptUniversal() {
        session.channel.sendMessage("Do you want this trivia to be viewable " +
                "across all servers i'm in? Type yes or no.")
                .queue();
    }

    public void promptServers() {
        session.channel.sendMessage("Enter the names all of the servers we're " +
                        "both in that you want this trivia to be" +
                        " playable in. Use a comma-separated list e.g. \n\n" +
                        "my test server, The Clubhouse, Jim's Hangout Server")
                .queue();
    }

    public void promptEditors() {
        session.channel.sendMessage("Enter the discord usernames of all " +
                        "people you want to be able to " +
                        "contribute to this trivia. These people will be able to edit, modify," +
                        "contribute to it, or delete it. Use a comma-separated list e.g.:\n\n" +
                        "bob41, mike10, awesomeUser")
                .queue();
    }

    public void promptQuestion() {
        session.channel.sendMessage("Enter a question you would like " +
                        "to ask in this trivia. Or type" +
                        "stop if you are finished")
                .queue();
    }

    public void promptAnswers() {
        session.channel.sendMessage("Now enter any correct answers to the question " +
                        "you asked. Use a comma-separated list e.g.\n\n" +
                        "mario kart wii, mkw, mkwii")
                .queue();
    }

    public void promptPoints() {
        session.channel.sendMessage("Enter how many points this question is worth. " +
                "Allowed amount is between 1 and 3 points")
                .queue();
    }

    public void promptMoreQuestions() {
        session.channel.sendMessage("Do you wish to input another question? " +
                "Type yes or no")
                .queue();
    }
}
