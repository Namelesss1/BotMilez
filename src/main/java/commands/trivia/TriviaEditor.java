package commands.trivia;

import java.util.List;

public class TriviaEditor {

    /**
     * Creates a new trivia type to add to the bot
     * @param name name of trivia
     * @param author creator of trivia
     * @param tags strings that help identify trivia
     * @param is_default whether custom or default trivia
     * @param universal true if trivia applies across all servers, false if not
     * @param serverIds id of servers that are allowed to use this trivia
     * @param allowedEditorNames usernames of all users allowed to edit this trivia
     * @return true upon successful creation
     */
    public static boolean createNewTrivia(String name, String author,List<String> tags,
                                  boolean is_default, boolean universal, List<Long> serverIds,
                                          List<String> allowedEditorNames) {


        return true;
    }

    /**
     * Deletes an entire trivia type from bot.
     * Only the original creator is allowed to delete the trivia.
     *
     * @param name name of trivia to delete
     * @return true upon success
     */
    public static boolean deleteTrivia(String name) {
        return true;
    }

    /**
     * Deletes a specific question from a trivia type.
     * Only original creator and allowed editors
     * of said trivia is allowed to do so.
     *
     * @param name name of trivia
     * @param id id of question to delete
     * @return true upon success
     */
    public static boolean deleteQuestionFrom(String name, long id) {
        return true;
    }

    /**
     * Adds a question to a trivia type. Only original creator and allowed
     * editors of said trivia is allowed to do so.
     *
     * @param name name of trivia
     * @param question the question
     * @param answer answer to the question
     * @param points how many points this question is worth.
     * @return true upon success
     */
    public static boolean addQuestionTo(String name, String question, String answer, long points) {
        return true;
    }

    /**
     * Removes an allowed user editor from a trivia.
     *
     * @param name trivia name
     * @param userToAdd name of user to remove
     * @return true upon success
     */
    public static boolean removeEditorFrom(String name, String userToAdd) {
        return true;
    }


    /**
     * Adds an allowed user editor to a trivia
     *
     * @param name name of trivia
     * @param userToAdd name of user to allow
     * @return true upon success
     */
    public static boolean addEditorTo(String name, String userToAdd) {
        return true;
    }

    /**
     * Renames an existing trivia to a new name.
     * @param oldName current name of trivia to modify
     * @param newName new name to name the trivia.
     * @return true upon success
     */
    public static boolean renameTo(String oldName, String newName) {
        return true;
    }

    /**
     * Sets the trivia to be able to be viewed by all servers, or only allowed servers
     * @param name name of trivia
     * @param isUniversal true if accessible through all servers, false if only select ones.
     * @return true upon success
     */
    public static boolean setUniversal(String name, boolean isUniversal) {
        return true;
    }

    /**
     * Adds to the allowed servers that can access the trivia
     * @param name name of trivia
     * @param serverId id of server to allow
     * @return true upon success
     */
    public static boolean addServerTo(String name, long serverId) {
        return true;
    }

    /**
     * Removes from allowed servers that can access the trivia
     * @param name name of trivia
     * @param serverId id of server to remove
     * @return true upon success
     */
    public static boolean removeServerFrom(String name, long serverId) {
        return true;
    }

    /**
     * Replace old existing tags in trivia type to the new ones.
     * @param name name of trivia
     * @param newTags list of strings representing new tags
     * @return true upon success
     */
    public static boolean replaceTags(String name, List<String> newTags) {
        return true;
    }

    /**
     * Adds a new tag to the existing list of tags.
     * @param name name of trivia
     * @param tag tag to add to list
     * @return true upon success
     */
    public static boolean addTagTo(String name, String tag) {
        return true;
    }

    /**
     * Removes an existing tag from list of tags
     * @param name name of trivia
     * @param tag tag to add to list
     * @return true upon success
     */
    public static boolean removeTagFrom(String name, String tag) {
        return true;
    }

}
