package commands.trivia;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a specific type of trivia in-use
 *
 * Makes use of json files to retrieve questions, answers, and metadata
 * related to these such as how many points a question is worth.
 * A trivia file contains the following info:
 * name (used as id)
 * tags (list of strings to identify the trivia and to help group multiple ones)
 * is_default (boolean to differentiate if this is a custom trivia made by someone, or default)
 * trivia_author (creator of the trivia question came from)
 * qas (pairs of questions, corresponding answers, corresponding points worth)
 */
public class TriviaType {
    private String name;
    private String author;
    private boolean is_default;
    private List<String> tags;

    private boolean all_servers;
    private List<Long> servers;
    private List<String> allowed_editors;

    private List<QA> questions;
    private int size;

    /**
     * Initializes a trivia type object representing a type of
     * questions that will be asked.
     *
     * @param triviaObj jsonObject that contains all data of a trivia type
     */
    public TriviaType(JSONObject triviaObj) {
        name = (String)triviaObj.get("name");
        author = (String)triviaObj.get("author");
        is_default = (boolean)triviaObj.get("is_default");
        tags = (JSONArray)triviaObj.get("tags");
        all_servers = (boolean)triviaObj.get("all_servers");
        servers = (JSONArray)triviaObj.get("servers");
        allowed_editors = (JSONArray)triviaObj.get("allowed_editors");
        questions = new ArrayList<>();

        /* Set metadata for questions and answers */
        JSONArray qaArray = (JSONArray)triviaObj.get("qas");
        for (int i = 0; i < qaArray.size(); i++) {
            JSONObject quesObj = (JSONObject)qaArray.get(i);
            long id = (long)quesObj.get("id");
            String question = (String)quesObj.get("question");
            List<String> answers = (JSONArray)quesObj.get("answer");
            long points = (long)quesObj.get("points");

            questions.add(new QA(id, question, answers, points));
        }

        size = qaArray.size();
    }

    public TriviaType(String user) {
        author = user;
    }

    /**
     * Sets name of this trivia
     * @param name name of trivia
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @param author sets the name of the creator of this trivia
     */
    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     *
     * @param universal sets whether this trivia is able to be viewed across all servers or not.
     */
    public void setUniversal(boolean universal) {
        all_servers = universal;
    }

    /**
     *
     * @param tags sets the tags for this trivia
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     *
     * @param servers sets allowed servers for this trivia
     */
    public void setServers(List<Long> servers) {
        this.servers = servers;
    }

    /**
     *
     * @param editors sets allowed editors/contributors to this trivia
     */
    public void setEditors(List<String> editors) {
        allowed_editors = editors;
    }

    /**
     * @return name of this trivia type
     */
    public String getName() {
        return name;
    }

    /**
     * @return creator of this trivia type
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return whether this is a custom trivia type (false) or default (true)
     */
    public boolean isDefault() {
        return is_default;
    }

    /**
     * @return list of strings that help represent this trivia type
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @return A list of QA objects that contain metadata for each question in this trivia type
     */
    public List<QA> getQuestions() {
        return questions;
    }

    /**
     * @param index index to retrieve from
     * @return the question at the current index
     */
    public String getQuestionAt(int index) {
        return questions.get(index).getQuestion();
    }

    /**
     * @param index index to retrieve from
     * @return the answers at the current index
     */
    public List<String> getAnswersAt(int index) {
        return questions.get(index).getAnswers();
    }

    /**
     * @param index index to retrieve from
     * @return the amount of points the question is worth at current index
     */
    public long getPointsAt(int index) {
        return questions.get(index).getPoints();
    }

    /**
     * @return how many questions are within this triviatype
     */
    public int getSize() {
        return size;
    }

    /**
     * Removes a question from this triviaType.
     * @param index which index of questions to remove from
     */
    public void removeQuestion(int index) {
        questions.remove(index);
        size--;
    }

    /**
     * @return whether this trivia type can be seen across all servers or not
     */
    public boolean isUniversal() {
        return all_servers;
    }

    /**
     * @return A list of all servers that can see this trivia type
     */
    public List<Long> getServers() {
        return servers;
    }

    /**
     * @return A list of all allowed editors of this trivia type
     */
    public List<String> getEditors() {
        return allowed_editors;
    }


    /**
     * @return the next available unique id for a question
     */
    public long getNextQuestionId() {

        /* Look for the next available id, taking into account potential gaps
         * in ids due to question removal
         */
        long availableId = 0;
        boolean found = false;
        /* Loop through questions, look for an available id */
        for (int i = 0; i < questions.size(); i++) {
            long id = questions.get(i).getId();
            if (availableId != id) {
                found = true;
                break;
            }
            availableId++;
        }

        /* If no gaps between ids, simply add it as the highest id */
        if (!found) {
            availableId = questions.size();
        }

        return availableId;
    }



}
