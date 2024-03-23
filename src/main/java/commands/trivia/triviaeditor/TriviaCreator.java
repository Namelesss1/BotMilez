package commands.trivia.triviaeditor;

import commands.trivia.QA;
import commands.trivia.Trivia;
import commands.trivia.TriviaType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import util.EmbedPageBuilder;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TriviaCreator {

    private TriviaEditSession session;


    public TriviaCreator(TriviaEditSession session) {
        this.session = session;

        /* Create new triviaType to represent the trivia. Then prompt the
         * user for the name of the new trivia
         */
        session.triviaType = new TriviaType(session.user.getJDA());
        session.triviaType.setAuthor(session.user.getName());
        session.inputType = TriviaEditSession.InputType.NAME;
        session.createScrollId = "trivia_create" + session.user.getName();
        promptName();
    }

    public void handleInput(Message message) {

        String input = message.getContentRaw();

        if (session.confirmState == TriviaEditSession.ConfirmState.CONFIRM) {
            processConfirm(input);
        }

        else if (session.inputType == TriviaEditSession.InputType.START) {
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
        else if (session.inputType == TriviaEditSession.InputType.IMAGE) {
            processImgInput(message);
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
        if (Trivia.triviaExists(name, true)) {
            session.channel.sendMessage("A trivia named " + name + " already exists! " +
                            "try another name.")
                    .queue();
            return;
        }

        session.channel.sendMessage("Your trivia name is: **" + name + "**" +
                "").queue();
        session.triviaType.setName(name);
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Processes user inputting tags for a trivia.
     * @param tagsStr tags to set
     */
    private void processTagsInput(String tagsStr) {
        List<String> tags =
                TriviaEditSession.removeDuplicateStringsFromList(
                        TriviaEditSession.parseCommaSeparatedList(tagsStr));
        session.channel.sendMessage("Your tags are: **" + tags.toString() + "**").queue();
        session.triviaType.setTags(tags);
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
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
        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Processes the user's input as to which servers to allow.
     *
     * @param serverStr User response representing all server names they want to allow.
     */
    private void processServersInput(String serverStr) {
        List<String> inputServers =
                TriviaEditSession.removeDuplicateStringsFromList(
                        TriviaEditSession.parseCommaSeparatedList(serverStr));
        List<String> serverIds = new ArrayList<>();
        for (Guild server : session.user.getMutualGuilds()) {
            if (inputServers.stream().anyMatch(server.getName()::equalsIgnoreCase)) {
                serverIds.add(server.getId());
            }
        }
        session.triviaType.setServers(serverIds);

        List<String> serverNames = new ArrayList<>();
        for (String serverId : serverIds) {
            serverNames.add(session.user.getJDA().getGuildById(serverId).getName());
        }
        session.channel.sendMessage("Your trivia will be viewable in the following servers: " +
                serverNames.toString()).queue();

        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }


    /**
     * Processes user's inputs of who they want to grant edit access to for the trivia.
     * @param editorStr string representing user's list of editors allowed
     */
    private void processEditorsInput(String editorStr) {
        List<String> editors = new ArrayList<>();
        editors.add(session.triviaType.getAuthor());
        if (!editorStr.trim().equalsIgnoreCase("none")) {
            List<String> additionalEditors =
                    TriviaEditSession.removeDuplicateStringsFromList(
                            TriviaEditSession.parseCommaSeparatedList(editorStr));
            editors.addAll(additionalEditors);
        }

        session.triviaType.setEditors(editors);
        session.channel.sendMessage("The following users will have " +
                "permission to edit your trivia: " +
                editors.toString()).queue();

        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
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
        session.channel.sendMessage("Question is: ```" + quesStr + "```")
                        .queue();

        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
    }

    /**
     * Processes support image input for a question. Ensures that an
     * image input is a valid url. No checks for whether link leads to
     * an image are done as of now, since this causes no problems in an
     * embed. Image simply won't appear as if one was not intentionally set.
     *
     * @param imgMsg user-input for an image url
     */
    private void processImgInput(Message imgMsg) {

        String imgInput = imgMsg.getContentRaw();

        boolean validImg = true;

        /* Validate URL */
        try {
            new URL(imgInput).toURI();
        }
        catch (Exception e) {
            validImg = false;
        }

        if (validImg) {
            session.questionObj.setImgURL(imgInput);
            session.channel.sendMessage("Your image is: ").queue();
            session.channel.sendMessage(imgInput).queue();
        }
        else {
            /* Check if image was sent as an attachment */
            List<Message.Attachment> attachments = imgMsg.getAttachments();
            if (attachments != null && !attachments.isEmpty()) {
                Message.Attachment attachment = attachments.get(0);
                if (attachment.getContentType().startsWith("image/")) {
                    session.questionObj.setImgURL(attachment.getUrl());
                    session.channel.sendMessage("Your image is: ").queue();
                    session.channel.sendMessage(attachment.getUrl()).queue();
                }
                else {
                    session.channel.sendMessage("Either your message attachment is not an image" +
                            ", or you chose not to add an image.").queue();
                }
            }
            else {
                session.channel.sendMessage("Either your image didn't work, or you chose " +
                        "not to add an image.").queue();
            }
        }

        promptConfirm();
        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
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

        if (response.trim().equalsIgnoreCase("yes")) {
            promptQuestion();
            session.inputType = TriviaEditSession.InputType.QUESTION;
            return;
        }

        else if (response.trim().equalsIgnoreCase("no")){
            boolean success = session.triviaType.writeTrivia(
                    session.path + "custom/" + session.triviaType.getName());
            if (success) {
                session.channel.sendMessage("Here is the trivia you created. " +
                        "You may edit it at any time if you want to modify something." +
                        " Thanks for making a trivia!").queue();
                session.channel.sendMessageEmbeds(session.triviaType.asEmbed()).queue();
                EmbedPageBuilder builder =  session.triviaType.asQuestionsEmbed(session.createScrollId);
                session.idToPageBuilder.put(session.createScrollId, builder);
                session.channel.sendMessageEmbeds(builder.build())
                        .setComponents().setActionRow(builder.getPageBuilderActionRow())
                        .queue();
                session.stop(session.user, session.channel);
            }
            else {
                session.channel.sendMessage("Oops, something went wrong when saving the" +
                        "trivia. Please try again later.").queue();
                session.stop(session.user, session.channel);
            }
        }

        else {
            session.channel.sendMessage("Invalid response. Please type yes or no.")
                    .queue();
        }
    }


    public void processConfirm(String input) {

        if (input.equalsIgnoreCase("yes")) {
            session.confirmState = TriviaEditSession.ConfirmState.NORMAL;
            if (session.inputType == TriviaEditSession.InputType.NAME) {
                promptTags();
                session.inputType = TriviaEditSession.InputType.TAGS;
            }
            else if (session.inputType == TriviaEditSession.InputType.TAGS) {
                promptUniversal();
                session.inputType = TriviaEditSession.InputType.UNIVERSAL;
            }
            else if (session.inputType == TriviaEditSession.InputType.UNIVERSAL) {
                if (session.triviaType.isUniversal()) {
                    promptEditors();
                    session.inputType = TriviaEditSession.InputType.EDITORS;
                }
                else {
                    promptServers();
                    session.inputType = TriviaEditSession.InputType.SERVERS;
                }
            }
            else if (session.inputType == TriviaEditSession.InputType.SERVERS) {
                promptEditors();
                session.inputType = TriviaEditSession.InputType.EDITORS;
            }
            else if (session.inputType == TriviaEditSession.InputType.EDITORS) {
                promptQuestion();
                session.inputType = TriviaEditSession.InputType.QUESTION;
            }
            else if (session.inputType == TriviaEditSession.InputType.QUESTION) {
                promptImg();
                session.inputType = TriviaEditSession.InputType.IMAGE;
            }
            else if (session.inputType == TriviaEditSession.InputType.IMAGE) {
                promptAnswers();
                session.inputType = TriviaEditSession.InputType.ANSWERS;
            }
            else if (session.inputType == TriviaEditSession.InputType.ANSWERS) {
                promptPoints();
                session.inputType = TriviaEditSession.InputType.POINTS;
            }
            else if (session.inputType == TriviaEditSession.InputType.POINTS) {
                session.triviaType.addQuestion(session.questionObj);
                session.channel.sendMessage("Here is your question:").queue();
                session.channel.sendMessageEmbeds(session.questionObj.asEmbed()).queue();
                promptMoreQuestions();
                session.inputType = TriviaEditSession.InputType.FINISHED;
            }
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
            else if (session.inputType == TriviaEditSession.InputType.IMAGE) {
                promptImg();
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




    public void promptName() {
            session.channel.sendMessage("What would you like to name this new trivia?")
                    .queue();
    }



    public void promptTags() {
        session.channel.sendMessage("Enter any tags you want for this trivia. " +
                                "These tags help identify" +
                "what the trivia is about. Seperate each one with a comma. For example, " +
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

    public void promptEditors() {
        session.channel.sendMessage("Enter the discord usernames of all " +
                        "people you want to be able to " +
                        "contribute to this trivia. These people will be able to edit, modify, " +
                        "contribute to it, or delete it. Use a comma-separated list e.g.:\n\n" +
                        "bob41, mike10, awesomeUser")
                .queue();
    }

    public void promptQuestion() {
        session.channel.sendMessage("Enter a question you would like " +
                        "to ask in this trivia.")
                .queue();
    }

    public void promptImg() {
        session.channel.sendMessage("Enter a URL link to an image you'd like to go with" +
                " the question. Or you can paste the image." +
                " If you want no images, type 'none' or anything that is not a url.")
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

    public void promptConfirm() {
        session.channel.sendMessage("Is this okay? Type yes or no")
                .queue();
    }
}
