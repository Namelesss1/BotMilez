package commands.trivia.triviaeditor;

import commands.Stoppable;
import commands.trivia.Trivia;
import commands.trivia.TriviaType;
import util.IO;

import java.util.Set;

public class TriviaRemover {

    private TriviaEditSession session;

    private String confirmDeleteStr;

    public TriviaRemover(TriviaEditSession session) {
        this.session = session;
        promptTrivia();
    }

    public void handleInput(String input) {
        if (session.confirmState == TriviaEditSession.ConfirmState.CONFIRM) {
            processConfirm(input);
        }
        else if (session.inputType == TriviaEditSession.InputType.SELECT_TRIVIA) {
            processTriviaSelect(input);
        }
    }

    /**
     * If a user selected to delete an existing trivia, ensures the trivia
     * exists and then prompts whether or not their sure that the user wants to delete it.
     * Sends an error to the user if trivia does not exist, or they don't have permissions
     * to modify it and prompts them to try again.
     *
     * @param name name of trivia to modify
     */
    private void processTriviaSelect(String name) {
        if (!Trivia.triviaExists(name, true)) {
            session.channel.sendMessage("The trivia, " + name + " does not exist!" +
                            " Please try inputting another name.")
                    .queue();
            return;
        }

        /* Load the trivia if found */
        session.triviaType = new TriviaType(session.path + "/custom/" + name + ".json",
                session.user.getJDA());
        session.triviaType = new TriviaType(session.triviaType, session.user.getJDA());

        if (!session.triviaType.getAuthor().equalsIgnoreCase(session.user.getName())) {
            session.channel.sendMessage("You do not have permission to remove this trivia." +
                    " Only the original creator of the trivia can.").queue();
            session.stop(session.user, session.channel);
            return;
        }

        confirmDeleteStr = "I want to delete " + session.triviaType.getName() + " " +
                "and I confirm it.";

        session.confirmState = TriviaEditSession.ConfirmState.CONFIRM;
        promptConfirm();
    }

    public void processConfirm(String input) {
        if (input.equalsIgnoreCase(confirmDeleteStr)) {
            boolean deleteSuccess = IO.deleteFile(session.path +
                    "custom/" + session.triviaType.getName() + ".json");

            /* If failed to delete the previous file with old name, send error and
             * delete the new one.
             */
            if (!deleteSuccess) {
                session.channel.sendMessage("There was a problem when replacing the previous " +
                                "trivia's name file. Please try again.")
                        .queue();
            }
            else {
                session.channel.sendMessage("Trivia has been deleted successfully.")
                        .queue();
            }
        }

        session.stop(session.user, session.channel);
    }

    public void promptTrivia() {
        Set<String> allowedTrivias = TriviaEditSession.getAllowedTriviasForUser(
                session.user.getName(), true);
        if (allowedTrivias.isEmpty()) {
            session.channel.sendMessage("You have no trivias you are allowed to remove. If you would like " +
                            "to create your own, type the command again in the server and in DMs select 'create'. Or " +
                            "request permission to edit someone else's trivia from them.")
                    .queue();
            session.stop(session.user, session.channel);
            return;
        }

        session.channel.sendMessage("Here are the trivias that you are able to delete: " +
                        "```"+ allowedTrivias + "```")
                .queue();

        session.channel.sendMessage("Enter the name of the trivia you wish to delete")
                .queue();
    }


    public void promptConfirm() {
        session.channel.sendMessage("Are you absolutely sure you want to delete the trivia, " +
                session.triviaType.getName() + "? Once deleted, it is gone FOREVER. If you" +
                " do not want to proceed, type " + Stoppable.END + " or " + Stoppable.CANCEL +
                " or " + Stoppable.STOP + ", or anything else.\n\n" +
                "If you still want to delete this trivia, type exactly this:" +
                "```" + confirmDeleteStr + "```")
                .queue();
    }
}
