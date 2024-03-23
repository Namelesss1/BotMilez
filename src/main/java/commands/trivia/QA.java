package commands.trivia;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * This class represents all metadata representing a question in this trivia type
 */
public class QA  {
    private long id;
    private String question;
    private List<String> answers;
    private long points;
    private boolean was_asked;
    private String imgURL;

    public QA(long id, String ques, List<String> ans, long pts, String url) {
        this.id = id;
        question = ques;
        answers = ans;
        points = pts;
        imgURL = url;
        was_asked = false;
    }

    public QA(long id) {
        was_asked = false;
        imgURL = null;
        this.id = id;
    }

    public QA() {
        imgURL = null;
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
     * Sets a url for an image relating to the question
     * @param url url link of image to set.
     */
    public void setImgURL(String url) {
        imgURL = url;
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
     *
     * @return an image url relating to the question, or null if none.
     */
    public String getImgURL() {
        return imgURL;
    }

    /**
     * @return true if question was already asked, false if not
     */
    public boolean wasAsked() {
        return was_asked;
    }

    /**
     *
     * @return this question as an embed
     */
    public MessageEmbed asEmbed() {
        EmbedBuilder em = new EmbedBuilder();
        em.setAuthor("Id: " + getId());
        if (getQuestion().length() >= 246) {
            em.addField(
                    "Question",
                    getQuestion(),
                    false
            );
            em.addField(
                    "Answers",
                    getAnswers().toString(),
                    false
            );
        }
        else {
            em.setTitle("Question: " + getQuestion());
            em.setDescription("Answers: " + getAnswers().toString());
        }
        em.setFooter(getPoints() + " points");
        if (imgURL != null) {
            em.setImage(imgURL);
        }
        em.setColor(Color.MAGENTA);
        return em.build();
    }


    /**
     * A class that defines a comparator for a question object.
     * Question objects are sorted by their numerical ids.
     */
    public static class QAsorter implements Comparator<QA> {

        @Override
        public int compare(QA qa, QA other) {
            if (qa.id > other.id) {
                return 1;
            }
            if (qa.id < other.id) {
                return -1;
            }
            return 0;
        }
    }
}
