package commands.trivia.triviaeditor;

import commands.trivia.Trivia;
import commands.trivia.TriviaType;
import util.IO;

public class TriviaModifier {

    private TriviaEditSession session;

    public TriviaModifier(TriviaEditSession session) {
        this.session = session;
        promptTrivia();
    }

    public void handleInput(String input) {

        if (session.inputType == TriviaEditSession.InputType.SELECT_TRIVIA) {
            processTriviaSelect(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.SELECT_ADD_OR_REMOVE) {
            processAddOrRemove(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.SELECT_ELEMENT) {
            processElementSelect(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.NAME) {
            processNameInput(input);
        }
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
            session.channel.sendMessage("The trivia, " + name + " does not exist!" +
                            " Please try inputting another name.")
                    .queue();
            return;
        }

        /* Load the trivia if found */
        session.triviaType = new TriviaType(session.path + "/custom/" + name + ".json");
        System.out.println(session.triviaType.getName());
        session.inputType = TriviaEditSession.InputType.SELECT_ADD_OR_REMOVE;

        if (!session.triviaType.getEditors().contains(session.user.getName())) {
            session.channel.sendMessage("You do not have permission to edit this trivia." +
                    " Maybe you can request permission from the creator.").queue();
            session.stop(session.user, session.channel);
            return;
        }

        promptAddOrRemove();
    }


    private void processAddOrRemove(String response) {
        if (response.equalsIgnoreCase("add")||
                response.equalsIgnoreCase("adding")) {
            session.modifyAction = TriviaEditSession.ModifyAction.ADD;
            session.inputType = TriviaEditSession.InputType.SELECT_ELEMENT;
        }
        else if (response.equalsIgnoreCase("remove") ||
                response.equalsIgnoreCase("removing")) {
            session.modifyAction = TriviaEditSession.ModifyAction.REMOVE;
            session.inputType = TriviaEditSession.InputType.SELECT_ELEMENT;
        }
        else {
            session.channel.sendMessage("Invalid response. Type" +
                    " ```Add``` or ```Remove```")
                    .queue();
            return;
        }

        promptElement();
    }


    /**
     * Figures out what to do based on user's response to being prompted
     * what they want to modify. Gives the user an error if they type something
     * that was not a given option.
     *
     * @param element user input string representing what they want to modify
     */
    private void processElementSelect(String element) {
        if (element.equalsIgnoreCase("name")) {
            promptName();
            session.inputType = TriviaEditSession.InputType.NAME;
        }
        else if (element.equalsIgnoreCase("tags")) {
            promptTags();
            session.inputType = TriviaEditSession.InputType.TAGS;
        }
        else if (element.equalsIgnoreCase("universal")) {
            promptUniversal();
            session.inputType = TriviaEditSession.InputType.UNIVERSAL;
        }
        else if (element.equalsIgnoreCase("servers")) {
            promptServers();
            session.inputType = TriviaEditSession.InputType.SERVERS;
        }
        else if (element.equalsIgnoreCase("editors")) {
            promptEditors();
            session.inputType = TriviaEditSession.InputType.EDITORS;
        }
        else if (element.equalsIgnoreCase("questions")) {
            promptQuestion();
            session.inputType = TriviaEditSession.InputType.QUESTION;
        }
        else {
            session.channel.sendMessage(element + " is not recognized. Type" +
                            " exactly one of the above options")
                    .queue();
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

        String oldName = session.triviaType.getName();
        session.triviaType.setName(name);
        boolean success = session.triviaType.writeTrivia(
                session.path + "custom/" + session.triviaType.getName());

        /* If failed to create the new file */
        if (!success) {
            session.channel.sendMessage(" There was an error when saving the name" +
                    "to the trivia. Please try again")
                    .queue();
            session.stop(session.user, session.channel);
            return;
        }

        boolean deleteSuccess = IO.deleteFile(session.path + "custom/" + oldName + ".json");
        /* If failed to delete the previous file with old name, send error and
         * delete the new one.
         */
        if (!deleteSuccess) {
            session.channel.sendMessage("There was a problem when replacing the previous " +
                    "trivia's name file. Please try again.")
                    .queue();
            IO.deleteFile(session.path + "custom/" + session.triviaType.getName());
            session.stop(session.user, session.channel);
            return;
        }

        session.channel.sendMessage("Success: your new trivia name was changed " +
                "from " + oldName + " to " + session.triviaType.getName())
                .queue();

        session.stop(session.user, session.channel);

    }



    private void writeBack(String successMsg) {
        boolean success = session.triviaType.writeTrivia(
                session.path + "custom/" + session.triviaType.getName());
        if (success) {
            session.channel.sendMessage("Success: " + successMsg)
                    .queue();
            session.stop(session.user, session.channel);
        }
        else {
            session.channel.sendMessage("Oops, something went wrong."  +
                    " Please try again").queue();
        }
    }

    public void promptTrivia() {
        session.channel.sendMessage("Enter the name of the trivia you wish to edit")
                .queue();
    }

    public void promptElement() {
        session.channel.sendMessage("What would you like to modify? " +
                "Type any of the following:\n\n" +
                " ```Name```" +
                " ```Tags```" +
                " ```Universal```" +
                " ```Servers```" +
                " ```Editors```" +
                " ```Questions```")
                .queue();
    }

    public void promptAddOrRemove() {
        session.channel.sendMessage(" Are you adding or removing something?")
                .queue();
    }



    public void promptName() {
        session.channel.sendMessage("Enter the new name you wish to give the" +
                " trivia.")
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
