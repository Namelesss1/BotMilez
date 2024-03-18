package commands.trivia;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import util.EmbedPageBuilder;
import util.IO;

import java.awt.*;
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

    /* jda object - to retrieve information about server names */
    private JDA jda;
    private String name;
    private String author;
    private boolean is_default;
    private List<String> tags;

    private boolean all_servers;
    private List<String> servers;
    private List<String> allowed_editors;

    private List<QA> questions;


    /**
     * Initialize a trivia type object representing a type of
     * question asked
     *
     * @param path string representing path to read jsonObject from
     */
    public TriviaType(String path, JDA jda) {
        this((JSONObject)IO.readJson(path), jda);
    }


    /**
     * Initializes a trivia type object representing a type of
     * questions that will be asked.
     *
     * @param triviaObj jsonObject that contains all data of a trivia type
     */
    public TriviaType(JSONObject triviaObj, JDA jda) {
        name = (String)triviaObj.get("name");
        author = (String)triviaObj.get("author");
        is_default = (boolean)triviaObj.get("is_default");
        tags = (JSONArray)triviaObj.get("tags");
        all_servers = (boolean)triviaObj.get("all_servers");
        servers = (JSONArray)triviaObj.get("servers");
        allowed_editors = (JSONArray)triviaObj.get("allowed_editors");
        questions = new ArrayList<>();
        this.jda = jda;

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

    }

    public TriviaType(JDA jda) {

        this.jda = jda;
        questions = new ArrayList<>();
        tags = new ArrayList<>();
        servers = new ArrayList<>();
        allowed_editors = new ArrayList<>();
    }

    /**
     * Creates a new trivia type object as a copy of another
     * @param other other triviaType object to copy from
     */
    public TriviaType(TriviaType other, JDA jda) {
        this.jda = jda;
        name = other.getName();
        is_default = other.isDefault();
        author = other.getAuthor();
        all_servers = other.isUniversal();

        /* Copy tags over */
        tags = new ArrayList<>();
        for (String tag : other.getTags()) {
            tags.add(tag);
        }

        /* Copy servers over */
        servers = new ArrayList<>();
        for (String id : other.getServers()) {
            servers.add(id);
        }

        /* Copy editors over */
        allowed_editors = new ArrayList<>();
        for (String editor : other.getEditors()) {
            allowed_editors.add(editor);
        }

        /* Copy Question objects over */
        questions = new ArrayList<>();
        for (QA otherQA : other.getQuestions()) {
            QA qa = new QA();
            qa.setId(otherQA.getId());
            qa.setQuestion(otherQA.getQuestion());
            qa.setAnswer(otherQA.getAnswers());
            qa.setPoints(otherQA.getPoints());
            questions.add(qa);
        }
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
    public void setServers(List<String> servers) {
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
     * Adds a new question object to this trivia type
     * @param qa question to add.
     */
    public void addQuestion(QA qa) {
        questions.add(qa);
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
        return questions.size();
    }

    /**
     * Removes a question from this triviaType.
     * @param index which index of questions to remove from
     */
    public void removeQuestion(int index) {
        questions.remove(index);
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
    public List<String> getServers() {
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


    /**
     * @return this object in an MessageEmbed form
     */
    public MessageEmbed asEmbed() {
        EmbedBuilder em = new EmbedBuilder();

        em.setColor(Color.MAGENTA);
        em.setTitle(getName());
        em.setAuthor("Trivia by: " + getAuthor());

        em.addField(
                "Tags: ",
                getTags().toString(),
                false
        );
        em.addField(
                "is viewable across all servers? ",
                "" + isUniversal(),
                false
        );
        List<String> serverNames = new ArrayList<>();
        for (String id : servers) {
            serverNames.add(jda.getGuildById(id).getName());
        }
        em.addField(
                "Viewable in these servers: ",
                serverNames.toString(),
                false
        );
        em.addField(
                "Allowed editors: ",
                allowed_editors.toString(),
                false
        );
        em.addField(
                "Number of questions: ",
                "" + getSize(),
                false
        );

        return em.build();
    }


    /**
     * @param id id of the buttoncomponent that will be used to scroll
     * @return The questions in this object within an embed.
     */
    public EmbedPageBuilder asQuestionsEmbed(String id) {

        List<MessageEmbed.Field> fields = new ArrayList<>();

        for (QA qa : questions) {
            MessageEmbed.Field idField = new MessageEmbed.Field(
                    "",
                    "ID: " + qa.getId(),
                    true
            );
            MessageEmbed.Field quesField = new MessageEmbed.Field(
                    qa.getQuestion() + "(" + qa.getPoints() + " points)",
                    "answers = " + qa.getAnswers().toString(),
                    false
            );

            fields.add(idField);
            fields.add(quesField);
        }

        EmbedPageBuilder em = new EmbedPageBuilder(10, fields, false, id);

        em.setColor(Color.MAGENTA);
        em.setTitle("Questions in " + getName());

        return em;
    }


    /**
     * Converts this triviaType into a JSONObject then attempts
     * to write it as a JSON file at the specified path
     *
     * @param path name and directory of file to write back
     * @return true on success, false on an error
     */
    public boolean writeTrivia(String path) {

        JSONObject trivObj = new JSONObject();
        trivObj.put("name", name);
        trivObj.put("author", author);
        trivObj.put("is_default", is_default);
        trivObj.put("all_servers", all_servers);

        JSONArray servers = new JSONArray();
        for (String serverId : this.servers) {
            servers.add(serverId);
        }
        trivObj.put("servers", servers);

        JSONArray editors = new JSONArray();
        for (String editor : allowed_editors) {
            editors.add(editor);
        }
        trivObj.put("allowed_editors", editors);

        JSONArray tags = new JSONArray();
        for (String tag : this.tags) {
            tags.add(tag);
        }
        trivObj.put("tags", tags);

        JSONArray questions = new JSONArray();
        for (QA qa : this.questions) {
            JSONObject obj = new JSONObject();
            obj.put("id", qa.getId());
            obj.put("question", qa.getQuestion());
            obj.put("answer", qa.getAnswers());
            obj.put("points", qa.getPoints());
            questions.add(obj);
        }
        trivObj.put("qas", questions);

        return IO.writeJson(trivObj, path + ".json");

    }

}
