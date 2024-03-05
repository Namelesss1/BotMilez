package commands.trivia;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an instance of a specific type of trivia in-use
 */
public class TriviaType {
    private String name;
    private String author;
    private boolean is_default;
    private List<String> tags;

    private List<QA> questions;

    public TriviaType(JSONObject triviaObj) {
        name = (String)triviaObj.get("name");
        author = (String)triviaObj.get("author");
        is_default = (boolean)triviaObj.get("is_default");
        tags = (JSONArray)triviaObj.get("tags");
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
     * This class represents all metadata representing a question in this trivia type
     */
    private class QA {
        private long id;
        private String question;
        private List<String> answers;
        private long points;
        private boolean was_asked;

        public QA(long id, String ques, List<String> ans, long pts) {
            this.id = id;
            question = ques;
            answers = ans;
            points = pts;
            was_asked = false;
        }

        /**
         * Sets this question as already asked
         */
        public void set_asked() {
            was_asked = true;
        }

        /**
         * @return id of question
         */
        public long getId() {
            return id;
        }

        /**
         * @return String representing the question
         */
        public String getQuestion() {
            return question;
        }

        /**
         * @return list of possible answers to the question
         */
        public List<String> getAnswers() {
            return answers;
        }

        /**
         * @return how many points the question is worth
         */
        public long getPoints() {
            return points;
        }

        /**
         * @return true if question was already asked, false if not
         */
        public boolean wasAsked() {
            return was_asked;
        }
    }
}
