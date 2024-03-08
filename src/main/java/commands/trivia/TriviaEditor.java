package commands.trivia;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.IO;

import java.util.List;


public class TriviaEditor {

    private static String path = "resources/trivia/";
    private static String custom_path = path + "custom/";

    /**
     * Creates a new trivia type to add to the bot
     * @param name name of trivia
     * @param author creator of trivia
     * @param tagsIn strings that help identify trivia
     * @param is_default whether custom or default trivia
     * @param universal true if trivia applies across all servers, false if not
     * @param serverIds id of servers that are allowed to use this trivia
     * @param allowedEditorNames usernames of all users allowed to edit this trivia
     * @return true upon successful creation
     */
    public static boolean createNewTrivia(String name, String author,List<String> tagsIn,
                                  boolean is_default, boolean universal, List<Long> serverIds,
                                          List<String> allowedEditorNames) {

        JSONObject trivObj = new JSONObject();
        trivObj.put("name", name);
        trivObj.put("author", author);
        trivObj.put("is_default", is_default);
        trivObj.put("all_servers", universal);

        JSONArray servers = new JSONArray();
        for (long serverId : serverIds) {
            servers.add(serverId);
        }
        trivObj.put("servers", servers);

        JSONArray editors = new JSONArray();
        for (String editor : allowedEditorNames) {
            editors.add(editor);
        }
        trivObj.put("allowed_editors", editors);

        JSONArray tags = new JSONArray();
        for (String tag : tagsIn) {
            tags.add(tag);
        }
        trivObj.put("tags", tags);

        if (is_default) {
            return IO.writeJson(trivObj, path + name);
        }
        return IO.writeJson(trivObj, custom_path + name);

    }

    /**
     * Deletes an entire trivia type from bot.
     * Only the original creator is allowed to delete the trivia.
     *
     * Fails if unable to delete the file.
     *
     * @param name name of trivia to delete
     * @return true upon success
     */
    public static boolean deleteTrivia(String name) {
        if (!exists(name)) {
            return false;
        }

        if (IO.fileExists(path + name)) {
            return IO.deleteFile(path + name);
        }
        return IO.deleteFile(custom_path + name);
    }

    /**
     * Deletes a specific question from a trivia type.
     * Only original creator and allowed editors
     * of said trivia is allowed to do so.
     *
     * Fails if unable to write back json, or if
     * specified question with id is not found.
     *
     * @param name name of trivia
     * @param id id of question to delete
     * @return true upon success
     */
    public static boolean deleteQuestionFrom(String name, long id) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray qas = (JSONArray)trivObj.get("qas");
        long indexToRemove = -1;

        /* Look for the question with specified id, store index in indexToRemove */
        for (int i = 0; i < qas.size(); i++) {
            JSONObject qa = (JSONObject) qas.get(i);
            long qaId = (long)qa.get("id");
            if (id == qaId) {
                indexToRemove = i;
            }
        }

        /* if question found, remove it and re-write the json */
        if (indexToRemove != -1) {
            qas.remove(indexToRemove);
            trivObj.replace("qas", qas);
            if (IO.fileExists(path + name)) {
                return IO.writeJson(trivObj ,path + name);
            }
            return IO.writeJson(trivObj,custom_path + name);
        }

        return false;
    }

    /**
     * Adds a question to a trivia type. Only original creator and allowed
     * editors of said trivia is allowed to do so.
     *
     * Fails if unable to write back to json
     *
     * @param name name of trivia
     * @param question the question
     * @param answers answers to the question
     * @param points how many points this question is worth.
     * @return true upon success
     */
    public static boolean addQuestionTo(String name, String question, List<String> answers, long points) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray qas = (JSONArray)trivObj.get("qas");

        /* Look for the next available id, taking into account potential gaps
         * in ids due to question removal
         */
        long availableId = 0;
        boolean found = false;
        /* Loop through questions, look for an available id */
        for (int i = 0; i < qas.size(); i++) {
            JSONObject qa = (JSONObject) qas.get(i);
            long id = (long)qa.get("id");
            if (availableId != id) {
                found = true;
                break;
            }
            availableId++;
        }

        /* If no gaps between ids, simply add it as the highest id */
        if (!found) {
            availableId = qas.size();
        }

        /* Package the new question into the json object, and write back */
        JSONObject newQA = new JSONObject();
        newQA.put("id", availableId);
        newQA.put("question", question);

        JSONArray answerArray = new JSONArray();
        for (String ans : answers) {
            answerArray.add(ans);
        }
        newQA.put("answer", answerArray);
        newQA.put("points", points);

        qas.add(newQA);
        trivObj.replace("qas", qas);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);

    }

    /**
     * Removes an allowed user editor from a trivia.
     * Fails if unable to write back json, or if the editor list
     * does not contain the requested user to remove.
     *
     * @param name trivia name
     * @param userToRemove name of user to remove
     * @return true upon success
     */
    public static boolean removeEditorFrom(String name, String userToRemove) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray editors = (JSONArray)trivObj.get("allowed_editors");
        if (!editors.contains(userToRemove)) {
            return false;
        }

        editors.remove(userToRemove);
        trivObj.replace("allowed_editors", editors);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }


    /**
     * Adds an allowed user editor to a trivia
     * Fails if unable to write back to json file, or
     * if userToAdd already exists in the users to allow.
     *
     * @param name name of trivia
     * @param userToAdd name of user to allow
     * @return true upon success
     */
    public static boolean addEditorTo(String name, String userToAdd) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray editors = (JSONArray)trivObj.get("allowed_editors");

        if (editors.contains(userToAdd)) {
            return false;
        }

        editors.add(userToAdd);
        trivObj.replace("allowed_editors", editors);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }

    /**
     * Renames an existing trivia to a new name. Since the JSON file
     * itself is named after the name field, a new file will be created
     * with the same name as newName. If successful, the file with the
     * oldName is deleted.
     *
     * Fails if unable to write back json, or if the file with the old name
     * is unable to be deleted.
     *
     * @param oldName current name of trivia to modify
     * @param newName new name to name the trivia.
     * @return true upon success
     */
    public static boolean renameTo(String oldName, String newName) {
        if (!exists(oldName)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(oldName);
        trivObj.replace("name", newName);

        /* Ensure successful write before deleting the file with the old name */
        boolean isDefault = (boolean)trivObj.get("is_default");
        boolean writeSuccess;
        if (isDefault) {
            writeSuccess = IO.writeJson(trivObj, path + newName);
        }
        else {
            writeSuccess = IO.writeJson(trivObj, custom_path + newName);
        }

        if (!writeSuccess) {
            return false;
        }

        boolean deleteSuccess = deleteTrivia(oldName);
        if (deleteSuccess) {
            return true;
        }
        /* If new file created but deletion failed, delete the new file and return false */
        else {
            deleteTrivia(newName);
            return false;
        }

    }

    /**
     * Sets the trivia to be able to be viewed by all servers, or only allowed servers
     * Fails if unable to write back json.
     * @param name name of trivia
     * @param isUniversal true if accessible through all servers, false if only select ones.
     * @return true upon success
     */
    public static boolean setUniversal(String name, boolean isUniversal) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        trivObj.replace("all_servers", isUniversal);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }

    /**
     * Adds to the allowed servers that can access the trivia
     * Fails if unable to write back to json file, or
     * added server already exists in allowed servers
     *
     * @param name name of trivia
     * @param serverId id of server to allow
     * @return true upon success
     */
    public static boolean addServerTo(String name, long serverId) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray servers = (JSONArray)trivObj.get("servers");

        if (servers.contains(serverId)) {
            return false;
        }

        servers.add(serverId);
        trivObj.replace("servers", servers);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);

    }

    /**
     * Removes from allowed servers that can access the trivia
     *
     * Fails if unable to write back json, or if server to remove
     * is not found.
     *
     * @param name name of trivia
     * @param serverId id of server to remove
     * @return true upon success
     */
    public static boolean removeServerFrom(String name, long serverId) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray servers = (JSONArray)trivObj.get("servers");
        long indexToRemoveFrom = servers.indexOf(serverId);
        if (indexToRemoveFrom == -1) {
            return false;
        }

        servers.remove(indexToRemoveFrom);
        trivObj.replace("servers", servers);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }

    /**
     * Replace old existing tags in trivia type to the new ones.
     * Fails if unable to write back json
     *
     * @param name name of trivia
     * @param newTags list of strings representing new tags
     * @return true upon success
     */
    public static boolean replaceTags(String name, List<String> newTags) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray tags = new JSONArray();
        tags.addAll(newTags);
        trivObj.replace("tags", tags);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }

    /**
     * Adds a new tag to the existing list of tags.
     * Fails if unable to write back json, or if the chosen
     * tag already exists.
     *
     * @param name name of trivia
     * @param tag tag to add to list
     * @return true upon success
     */
    public static boolean addTagTo(String name, String tag) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray tags = (JSONArray)trivObj.get("tags");
        if (tags.contains(tag)) {
            return false;
        }
        tags.add(tag);
        trivObj.replace("tags", tags);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }

    /**
     * Removes an existing tag from list of tags
     * Fails if unable to write back json, or if
     * the chosen tag is not found.
     *
     * @param name name of trivia
     * @param tag tag to add to list
     * @return true upon success
     */
    public static boolean removeTagFrom(String name, String tag) {
        if (!exists(name)) {
            return false;
        }

        JSONObject trivObj = getTriviaObj(name);
        JSONArray tags = (JSONArray)trivObj.get("tags");
        long indexToRemove = tags.indexOf(tag);
        if (indexToRemove == -1) {
            return false;
        }
        tags.remove(indexToRemove);
        trivObj.replace("tags", tags);

        if (IO.fileExists(path + name)) {
            return IO.writeJson(trivObj ,path + name);
        }
        return IO.writeJson(trivObj,custom_path + name);
    }


    /**
     * Helper method to determine if a trivia exists by name.
     * @param name name of trivia
     * @return true if exists, false if not.
     */
    private static boolean exists(String name) {
        if (IO.fileExists(path + name) || IO.fileExists(custom_path + name)) {
            return true;
        }
        return false;
    }

    /**
     * Helper method to determine if a trivia is allowed to be edited by user.
     * @param name name of trivia
     * @param username name of user requesting edit
     * @return true if user is allowed, false if not.
     */
    public static boolean editAllowed(String name, String username) {
        JSONObject obj;
        if (IO.readJson(path + name) != null) {
            obj = (JSONObject)IO.readJson(path + name);
        }
        else {
            obj = (JSONObject)IO.readJson(custom_path + name);
        }

        List<String> editors = (JSONArray)obj.get("allowed_editors");
        if (editors.contains(username)) {
            return true;
        }
        return false;
    }

    /**
     * Helper method to obtain a JSONObject for an existing trivia
     * @param name name of trivia
     * @return JSONObject representing the trivia with the given name.
     */
    private static JSONObject getTriviaObj(String name) {
        JSONObject obj;
        if (IO.readJson(path + name) != null) {
            obj = (JSONObject)IO.readJson(path + name);
        }
        else {
            obj = (JSONObject)IO.readJson(custom_path + name);
        }

        return obj;
    }

}
