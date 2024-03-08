package commands.trivia;

import java.util.List;

public class TriviaEditor {

    /**
     * Creates a new trivia type to add to the bot
     * @param name name of trivia
     * @param author creator of trivia
     * @param tags strings that help identify trivia
     * @param is_default whether custom or default trivia
     * @return true upon successful creation
     */
    public static boolean createNewTrivia(String name, String author,List<String> tags,
                                  boolean is_default,) {


        return true;
    }

    /**
     * Deletes an entire trivia type from bot.
     * Only the original creator is allowed to delete the trivia.
     *
     * @param name name of trivia to delete
     * @param username name of user requesting to delete trivia
     * @return true upon success
     */
    public static boolean deleteTrivia(String name, String username) {
        return true;
    }

    /**
     * Deletes a specific question from a trivia type.
     * Only original creator of said trivia is allowed to do so.
     *
     * @param name name of trivia
     * @param username name of user requesting to delete trivia  question
     * @param id id of question to delete
     * @return true upon success
     */
    public static boolean deleteQuestionFrom(String name, String username, long id) {
        return true;
    }

    /**
     * Adds a question to a trivia type. Only original creator of said trivia
     * is allowed to do so.
     *
     * @param name name of trivia
     * @param username username of user requesting to add a trivia question
     * @param question the question
     * @param answer answer to the question
     * @param points how many points this question is worth.
     * @return true upon success
     */
    public static boolean addQuestionTo(String name, String username,
                                        String question, String answer, long points) {
        return true;
    }

}
