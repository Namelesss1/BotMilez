package commands.trivia;

import java.util.List;

/**
 * This class represents all metadata representing a question in this trivia type
 */
public class QA {
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

    public QA(long id) {
        was_asked = false;
        this.id = id;
    }

    public QA() {
        was_asked = false;
    }


    /**
     * @param id sets the unique id of this question
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Sets the question prompt of this object
     * @param ques question prompt
     */
    public void setQuestion(String ques) {
        question = ques;
    }

    /**
     * Sets the corresponding answers of this object
     * @param answers list of all correct answers
     */
    public void setAnswer(List<String> answers) {
        this.answers = answers;
    }

    /**
     * Sets the points worth of this question
     * @param points how many points this question is worth
     */
    public void setPoints(long points) {
        this.points = points;
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
