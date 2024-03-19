package commands.trivia.triviaeditor;

import commands.trivia.QA;
import commands.trivia.Trivia;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import util.EmbedPageBuilder;
import util.IO;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TriviaModifier {

    /*
     * The calling TriviaEditSession. A lot of its variables are accessed
     * to assist with the modification process
     */
    private TriviaEditSession session;

    /* TriviaType resembling the original trivia being edited, but
     * contains the changes that the user made. This is mainly to preserve
     * the original trivia in case the user made a mistake in their input
     * that they wish to revert before writing back the final trivia */
    private TriviaType modifiedTrivia;



    public TriviaModifier(TriviaEditSession session) {
        this.session = session;
        session.modifyAction = TriviaEditSession.ModifyAction.NONE;
        session.modifyScrollId = "triviamodify" + session.user.getName();
        session.scrollQuestionId = "triviamodifyquestion" + session.user.getName();
        session.scrollUpdatedQuestionId = "triviaupdatedquestion" + session.user.getName();
        promptTrivia();

    }


    /**
     * Handles users input, and directs the next steps according to what
     * the state machines say
     * @param input user's input
     */
    public void handleInput(String input) {

        if (session.modifyAction == TriviaEditSession.ModifyAction.SELECT_ADD_OR_REMOVE) {
            processAddOrRemove(input);
        }
        else if (session.confirmState == TriviaEditSession.ConfirmState.CONFIRM) {
            processConfirm(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.SELECT_TRIVIA) {
            processTriviaSelect(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.SELECT_ELEMENT) {
            processElementSelect(input);
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
            processServerInput(input);
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
        session.triviaType = new TriviaType(session.path + "/custom/" + name + ".json",
                session.user.getJDA());
        modifiedTrivia = new TriviaType(session.triviaType, session.user.getJDA());
        session.inputType = TriviaEditSession.InputType.SELECT_ELEMENT;

        if (!session.triviaType.getEditors().contains(session.user.getName())
            && !session.triviaType.getAuthor().equalsIgnoreCase(session.user.getName())) {
            session.channel.sendMessage("You do not have permission to edit this trivia." +
                    " Maybe you can request permission from the creator.").queue();
            session.stop(session.user, session.channel);
            return;
        }

        promptElement();
    }


    /**
     * Processes whether to add or remove an element from the trivia.
     * Then it prompts the user about the selected element to add/remove
     *
     * @param response string containing "add" or "remove"
     */
    private void processAddOrRemove(String response) {
        if (response.equalsIgnoreCase("add")||
                response.equalsIgnoreCase("adding")) {
            session.modifyAction = TriviaEditSession.ModifyAction.ADD;
        }
        else if (response.equalsIgnoreCase("remove") ||
                response.equalsIgnoreCase("removing")) {
            session.modifyAction = TriviaEditSession.ModifyAction.REMOVE;
        }
        else {
            session.channel.sendMessage("Invalid response. Type" +
                    " ```Add``` or ```Remove```")
                    .queue();
            return;
        }

        if (session.inputType == TriviaEditSession.InputType.TAGS) {
            promptTags();
        }
        else if (session.inputType == TriviaEditSession.InputType.SERVERS) {
            promptServers();
        }
        else if (session.inputType == TriviaEditSession.InputType.EDITORS) {
            promptEditors();
        }
        else if (session.inputType == TriviaEditSession.InputType.QUESTION) {
            promptQuestion();
        }
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
            promptAddOrRemove("tags");
            session.modifyAction = TriviaEditSession.ModifyAction.SELECT_ADD_OR_REMOVE;
            session.inputType = TriviaEditSession.InputType.TAGS;
        }
        else if (element.equalsIgnoreCase("universal")) {
            promptUniversal();
            session.inputType = TriviaEditSession.InputType.UNIVERSAL;
        }
        else if (element.equalsIgnoreCase("servers")) {
            promptAddOrRemove("servers");
            session.modifyAction = TriviaEditSession.ModifyAction.SELECT_ADD_OR_REMOVE;
            session.inputType = TriviaEditSession.InputType.SERVERS;
        }
        else if (element.equalsIgnoreCase("editors")) {
            promptAddOrRemove("editors");
            session.modifyAction = TriviaEditSession.ModifyAction.SELECT_ADD_OR_REMOVE;
            session.inputType = TriviaEditSession.InputType.EDITORS;
        }
        else if (element.equalsIgnoreCase("questions")) {
            promptAddOrRemove("questions");
            session.modifyAction = TriviaEditSession.ModifyAction.SELECT_ADD_OR_REMOVE;
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
        modifiedTrivia.setName(name);

        session.channel.sendMessage("Your trivia will be renamed " +
                "from " + oldName + " to " + modifiedTrivia.getName())
                .queue();

        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Modifies the all_servers key of the trivia based on universal
     * Only accepts "yes" or "no"
     *
     * @param universal whether or not to set this as universal.
     */
    private void processUniversalInput(String universal) {
        boolean isUniversal;
        if (universal.equalsIgnoreCase("yes")) {
            isUniversal = true;
        }
        else if (universal.equalsIgnoreCase("no")) {
            isUniversal = false;
        }
        else {
            session.channel.sendMessage(universal + " is not recognized." +
                    " Please type yes or no.")
                    .queue();
            return;
        }

        modifiedTrivia.setUniversal(isUniversal);
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Processes user input to modify tags. Error if user attempts to remove a
     * non-existing tag, or adds a tag that is already existing
     *
     * @param input user comma-separated string listing servers
     */
    private void processTagsInput(String input) {

        Set<String> userInputTags = new HashSet<>(
                TriviaEditSession.removeDuplicateStringsFromList(
                        TriviaEditSession.parseCommaSeparatedList(input)));
        List<String> currElements = session.triviaType.getTags();

        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            List<String> correctToAdd = new ArrayList<>();

            /* Search for duplicates */
            for (String element : userInputTags) {
                if (currElements.stream().anyMatch(element::equalsIgnoreCase)) {
                    session.channel.sendMessage(element + " could not" +
                                    " be added to servers, it already is one!")
                            .queue();
                }
                else {
                    correctToAdd.add(element);
                }
            }

            currElements.addAll(correctToAdd);
        }
        else {
            List<String> correctToRemove = new ArrayList<>();

            for (String element : userInputTags) {
                if (currElements.stream().anyMatch(element::equalsIgnoreCase)) {
                    correctToRemove.add(element);
                }
                else {
                    session.channel.sendMessage(element + " could not" +
                                    " be removed from editors, it doesn't exist!")
                            .queue();
                }
            }

            currElements.removeAll(correctToRemove);
        }

        modifiedTrivia.setTags(currElements);
        session.channel.sendMessage(" Your new tags will be: " +
                modifiedTrivia.getTags())
                .queue();
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Processes user input to modify servers. Error if user attempts to remove a
     * non-existing server, adds a server that is already existing, or names
     * a server that is not mutual between them and the bot.
     *
     * @param input user comma-separated string listing servers
     */
    private void processServerInput(String input) {

        List<String> currElements = session.triviaType.getServers();
        Set<String> mutualServers = new HashSet<>();

        for (Guild server : session.user.getMutualGuilds()) {
            Set<String> userInputSet = new HashSet<>(
                    TriviaEditSession.removeDuplicateStringsFromList(
                            TriviaEditSession.parseCommaSeparatedList(input)));

            if (userInputSet.stream().anyMatch(server.getName()::equalsIgnoreCase)) {
                mutualServers.add(server.getId());
            }
        }

        if (mutualServers.isEmpty()) {
            session.channel.sendMessage("None of the names you listed are mutual servers between us." +
                    " Please try again.")
                    .queue();
            return;
        }

        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            List<String> correctToAdd = new ArrayList<>();

            /* Search for duplicates */
            for (String element : mutualServers) {
                if (currElements.stream().anyMatch(element::equalsIgnoreCase)) {
                    session.channel.sendMessage(session.user.getJDA().getGuildById(element).getName() + " could not" +
                                    " be added to servers, it already is one!")
                            .queue();
                }
                else {
                    correctToAdd.add(element);
                }
            }

            currElements.addAll(correctToAdd);
        }
        else {
            List<String> correctToRemove = new ArrayList<>();

            for (String element : mutualServers) {
                if (currElements.stream().anyMatch(element::equalsIgnoreCase)) {
                    correctToRemove.add(element);
                }
                else {
                    session.channel.sendMessage(session.user.getJDA().getGuildById(element).getName() + " could not" +
                                    " be removed from servers, it doesn't exist!")
                            .queue();
                }
            }

            currElements.removeAll(correctToRemove);
        }

        modifiedTrivia.setServers(currElements);
        Set<String> serverNames = new HashSet<>();
        for (String serverId : modifiedTrivia.getServers()) {
            serverNames.add(session.user.getJDA().getGuildById(serverId).getName());
        }
        session.channel.sendMessage("Your new servers will be: " +
                serverNames)
                        .queue();
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }

    /**
     * Processes user input to modify editors. Error if user attempts to remove a
     * non-existing editor, or adds an editor that is already existing.
     *
     * @param input user comma-separated string listing editors
     */
    private void processEditorsInput(String input) {

        Set<String> userInputEditors = new HashSet<>(
                TriviaEditSession.removeDuplicateStringsFromList(
                        TriviaEditSession.parseCommaSeparatedList(input)));
        List<String> currElements = session.triviaType.getEditors();

        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            List<String> correctToAdd = new ArrayList<>();

            /* Search for duplicates */
            for (String element : userInputEditors) {
                if (currElements.stream().anyMatch(element::equalsIgnoreCase)) {
                    session.channel.sendMessage(element + " could not" +
                                    " be added to editors, it already is one!")
                            .queue();
                }
                else {
                    correctToAdd.add(element);
                }
            }

            currElements.addAll(correctToAdd);
        }
        else {
            List<String> correctToRemove = new ArrayList<>();

            for (String element : userInputEditors) {
                if (currElements.stream().anyMatch(element::equalsIgnoreCase)) {
                    correctToRemove.add(element);
                }
                else {
                    session.channel.sendMessage(element + " could not" +
                                    " be removed from editors, it doesn't exist!")
                            .queue();
                }
            }

            currElements.removeAll(correctToRemove);
        }

        modifiedTrivia.setEditors(currElements);
        session.channel.sendMessage(" Your new editors will be: " +
                        modifiedTrivia.getEditors())
                .queue();
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }

    /**
     * Processes questions that the user decides to input
     *
     * @param quesStr string representing question prompt user wants to ask
     */
    private void processQuestionInput(String quesStr) {
        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            session.questionObj = new QA();
            session.questionObj.setId(modifiedTrivia.getNextQuestionId());
            session.questionObj.setQuestion(quesStr);
            session.channel.sendMessage("Question is: ```" + quesStr + "```")
                    .queue();

            promptConfirm();
            session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
        }

        else {
            List<String> userInput =
                    TriviaEditSession.removeDuplicateStringsFromList(
                            TriviaEditSession.parseCommaSeparatedList(quesStr));
            Set<Long> questionsIdsToRemove = new HashSet<>();
            Set<QA> questionsToRemove = new HashSet<>();
            List<QA> questionsList = session.triviaType.getQuestions();

            /* Convert each string to integer */
            for (String num : userInput) {
                try {
                    Long quesNum = Long.parseLong(num);
                    questionsIdsToRemove.add(quesNum);
                }
                catch (NumberFormatException e) {
                    session.channel.sendMessage(num + " is not an integer. " +
                            "please input a number (the id of the question you want to remove)")
                            .queue();
                    return;
                }
            }

            for (Long id : questionsIdsToRemove) {
                boolean success = modifiedTrivia.removeQuestionById(id);
                if (!success) {
                    session.channel.sendMessage("The question id " + id + " is not" +
                            "valid. Failed to remove this question")
                            .queue();

                }
                else {
                    session.channel.sendMessage("Removed question with id " + id)
                            .queue();
                }
            }

            promptConfirm();
            session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
        }

    }


    /**
     * Processes answers that the user wants to set for a corresponding question prompt
     * @param ansStr string representing a list of answers the user wants
     */
    private void processAnswerInput(String ansStr) {
        List<String> answers = TriviaEditSession.parseCommaSeparatedList(ansStr);
        session.questionObj.setAnswer(answers);
        session.channel.sendMessage("Answers to your question are: " +
                        "```" + answers + "```")
                .queue();

        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
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
        session.channel.sendMessage("This question is worth " + pts + " points.")
                .queue();
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Processes a user's response to being asked if they want to input more questions to a trivia.
     * Answer must be "yes" or "no", otherwise error message sent to user so they can try again.
     *
     * @param response user's response to prompt whether they want to continue inputting more questions.
     */
    public void processMoreQuestionPrompt(String response) {
        session.channel.sendMessage("Here is your question:").queue();
        session.channel.sendMessageEmbeds(session.questionObj.asEmbed()).queue();

        if (response.trim().equalsIgnoreCase("yes")) {
            promptQuestion();
            session.inputType = TriviaEditSession.InputType.QUESTION;
            return;
        }
        else if (response.trim().equalsIgnoreCase("no")) {
            boolean success = modifiedTrivia.writeTrivia(
                    session.path + "custom/" + session.triviaType.getName());
            if (success) {
                session.stop(session.user, session.channel);
            }
            else {
                session.channel.sendMessage("Oops, something went wrong when saving the" +
                        "trivia. Please try again later.").queue();
                session.stop(session.user, session.channel);
            }
        }
        else {
            session.channel.sendMessage("Invalid response. Please type yes or no")
                    .queue();
        }



    }


    public void processConfirm(String input) {

        session.confirmState = TriviaEditSession.ConfirmState.NORMAL;

        if (input.equalsIgnoreCase("yes")) {

            if (session.inputType == TriviaEditSession.InputType.NAME) {
                boolean success = modifiedTrivia.writeTrivia(
                        session.path + "custom/" + modifiedTrivia.getName());

                /* If failed to create the new file */
                if (!success) {
                    session.channel.sendMessage(" There was an error when saving the name" +
                                    "to the trivia. Please try again")
                            .queue();
                    session.stop(session.user, session.channel);
                    return;
                }

                boolean deleteSuccess = IO.deleteFile(session.path + "custom/" + session.triviaType.getName() + ".json");
                /* If failed to delete the previous file with old name, send error and
                 * delete the new one.
                 */
                if (!deleteSuccess) {
                    session.channel.sendMessage("There was a problem when replacing the previous " +
                                    "trivia's name file. Please try again.")
                            .queue();
                    IO.deleteFile(session.path + "custom/" + modifiedTrivia.getName());
                    session.stop(session.user, session.channel);
                    return;
                }
            }
            else if (session.inputType == TriviaEditSession.InputType.TAGS) {
                writeBack("your new tags have been updated.");
            }
            else if (session.inputType == TriviaEditSession.InputType.UNIVERSAL) {
                writeBack(" your trivia is viewable across all servers i'm in: "
                        + modifiedTrivia.isUniversal());
            }
            else if (session.inputType == TriviaEditSession.InputType.SERVERS) {
                writeBack("your new servers have been updated.");
            }
            else if (session.inputType == TriviaEditSession.InputType.EDITORS) {
                writeBack("your new editors have been updated. ");
            }
            else if (session.inputType == TriviaEditSession.InputType.QUESTION) {
                if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
                    session.inputType = TriviaEditSession.InputType.ANSWERS;
                    promptAnswers();
                    return;
                }
                else {
                    writeBack("Success. Any questions with valid ids have been removed.");
                }
            }
            else if (session.inputType == TriviaEditSession.InputType.ANSWERS) {
                session.inputType = TriviaEditSession.InputType.POINTS;
                promptPoints();
                return;
            }
            else if (session.inputType == TriviaEditSession.InputType.POINTS) {
                session.inputType = TriviaEditSession.InputType.FINISHED;
                modifiedTrivia.addQuestion(session.questionObj);
                promptMoreQuestions();
                return;
            }

            session.stop(session.user, session.channel);


        }

        else if (input.equalsIgnoreCase("no")) {
            session.confirmState = TriviaEditSession.ConfirmState.NORMAL;

            if (session.inputType == TriviaEditSession.InputType.NAME) {
                promptName();
            }
            else if (session.inputType == TriviaEditSession.InputType.TAGS) {
                promptTags();
            }
            else if (session.inputType == TriviaEditSession.InputType.UNIVERSAL) {
                promptUniversal();
            }
            else if (session.inputType == TriviaEditSession.InputType.SERVERS) {
                promptServers();
            }
            else if (session.inputType == TriviaEditSession.InputType.EDITORS) {
                promptEditors();
            }
            else if (session.inputType == TriviaEditSession.InputType.QUESTION) {
                promptQuestion();
            }
            else if (session.inputType == TriviaEditSession.InputType.ANSWERS) {
                promptAnswers();
            }
            else if (session.inputType == TriviaEditSession.InputType.POINTS) {
                promptPoints();
            }
        }

        else {
            session.channel.sendMessage(input + " is not a recognized answer. " +
                            "Please type yes or no.")
                    .queue();
        }
    }


    private void writeBack(String successMsg) {
        boolean success = modifiedTrivia.writeTrivia(
                session.path + "custom/" + modifiedTrivia.getName());
        if (success) {
            session.channel.sendMessage("Success: " + successMsg)
                    .queue();
        }
        else {
            session.channel.sendMessage("Oops, something went wrong."  +
                    " Please try again").queue();
        }
    }

    public void promptTrivia() {
        Set<String> allowedTrivias = TriviaEditSession.getAllowedTriviasForUser(session.user.getName());
        if (allowedTrivias.isEmpty()) {
            session.channel.sendMessage("You have no trivias you are allowed to edit. If you would like " +
                    "to create your own, type the command again in the server and in DMs select 'create'. Or " +
                    "request permission to edit someone else's trivia from them.")
                    .queue();
            session.stop(session.user, session.channel);
            return;
        }

        session.channel.sendMessage("Here are the trivias that you have access to: " +
                "```"+ allowedTrivias + "```")
                        .queue();

        session.channel.sendMessage("Enter the name of the trivia you wish to edit")
                .queue();
    }

    public void promptElement() {
        session.channel.sendMessage("Here is your trivia:").queue();
        session.channel.sendMessageEmbeds(session.triviaType.asEmbed())
                        .queue();
        EmbedPageBuilder builder = session.triviaType.asQuestionsEmbed(session.modifyScrollId);
        session.idToPageBuilder.put(session.modifyScrollId,builder);
        session.channel.sendMessageEmbeds(builder.build())
                .setComponents().setActionRow(builder.getPageBuilderActionRow())
                .queue();
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

    public void promptAddOrRemove(String element) {
        session.channel.sendMessage(" Are you adding or removing " + element + "?")
                .queue();
    }



    public void promptName() {
        session.channel.sendMessage("Enter the new name you wish to give the" +
                " trivia.")
                .queue();
    }

    public void promptTags() {
        session.channel.sendMessage("Here are the tags of the trivia: " +
                "```" + session.triviaType.getTags() + "```")
                .queue();

        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            session.channel.sendMessage("Enter any additional tags you want to" +
                    " add.")
                    .queue();
        }
        else {
            session.channel.sendMessage("Enter the names of all tags you " +
                    "want to remove.")
                    .queue();
        }
    }

    public void promptUniversal() {
        session.channel.sendMessage("Do you want this trivia to be viewable " +
                        "across all servers i'm in? Type yes or no.")
                .queue();
    }

    public void promptServers() {
        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            session.channel.sendMessage("Enter the names all of the servers we're " +
                            "both in that you want this trivia to be" +
                            " playable in. Use a comma-separated list e.g. \n\n" +
                            "my test server, The Clubhouse, Jim's Hangout Server")
                    .queue();
            session.channel.sendMessage("Here are our mutual servers: ").queue();
            EmbedBuilder emBuilder = new EmbedBuilder();
            emBuilder.setColor(Color.MAGENTA);
            emBuilder.setTitle("Servers");
            for (Guild server : session.user.getMutualGuilds()) {
                emBuilder.addField(new MessageEmbed.Field(
                        server.getName(),
                        "Mutual Server",
                        false
                ));
            }
            session.channel.sendMessageEmbeds(emBuilder.build()).queue();
        }
        else {
            List<String> serverIds = session.triviaType.getServers();
            List<String> serverNames = new ArrayList<>();
            for (String id : serverIds) {
                serverNames.add(session.user.getJDA().getGuildById(id).getName());
            }

            session.channel.sendMessage("Enter the names all of the servers that " +
                            "you wish to remove. Use a comma-separated list e.g. \n\n" +
                            "my test server, The Clubhouse, Jim's Hangout Server")
                    .queue();
            session.channel.sendMessage("Here are the current servers: " +
                    serverNames)
                    .queue();
        }

    }

    public void promptEditors() {
        session.channel.sendMessage("Here are all the current editors: " +
                        session.triviaType.getEditors())
                .queue();

        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            session.channel.sendMessage("Enter the discord usernames of all " +
                            "people you want to be able to " +
                            "contribute to this trivia. These people will be able to edit, modify," +
                            "contribute to it, or delete it. Use a comma-separated list e.g.:\n\n" +
                            "bob41, mike10, awesomeUser")
                    .queue();
        }
        else {
            session.channel.sendMessage("Enter the discord usernames of all " +
                            "people you want to remove edit access from. " +
                            "Use a comma-separated list e.g.:\n\n" +
                            "bob41, mike10, awesomeUser")
                    .queue();
        }


    }

    public void promptQuestion() {
        if (session.modifyAction == TriviaEditSession.ModifyAction.ADD) {
            session.channel.sendMessage("Enter a question you would like " +
                            "to ask in this trivia. ")
                    .queue();
        }

        else {
            session.channel.sendMessage("Here are all the questions in the trivia. type the ids " +
                            "of the questions you want to remove. Use a comma-separated list e.g.\n\n" +
                            "3, 7, 15 ")
                    .queue();
            EmbedPageBuilder builder = session.triviaType.asQuestionsEmbed(session.scrollQuestionId);
            session.idToPageBuilder.put(session.scrollQuestionId,builder);
            session.channel.sendMessageEmbeds(builder.build())
                    .setComponents().setActionRow(builder.getPageBuilderActionRow())
                    .queue();
        }

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

    public void promptConfirm() {
        session.channel.sendMessage("Is this okay? Type yes or no")
                .queue();
    }
}
