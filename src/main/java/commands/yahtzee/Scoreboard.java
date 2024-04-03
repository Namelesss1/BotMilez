package commands.yahtzee;

/**
 * This class represents a Yahtzee scoreboard.
 */
public class Scoreboard {

    /* Name of player */
    private String name;

    /* All the below represent the score for each of the individual slots */
    /* Value of -1 means the value has not been filled in yet */
    private int ones = -1;
    private int twos = -1;
    private int threes = -1;
    private int fours = -1;
    private int fives = -1;
    private int sixes = -1;
    private int threeOfAKind = -1;
    private int fourOfAKind = -1;
    private int yahtzee = -1;
    private int smallStraight = -1;
    private int largeStraight = -1;
    private int fullHouse = -1;

    /* Number of yahtzees obtained */
    private int yahtzeeCount = 0;


    public Scoreboard(String name) {
        this.name = name;
    }

    /**
     * Sets the ones score to the given score.
     * @param score score to set the ones to
     */
    public void setOnes(int score) {
        ones = score;
    }

    /**
     * Sets the twos score to the given score.
     * @param score score to set the twos to
     */
    public void setTwos(int score) {
        twos = score;
    }

    /**
     * Sets the threes score to the given score.
     * @param score score to set the threes to
     */
    public void setThrees(int score) {
        threes = score;
    }

    /**
     * Sets the fours score to the given score.
     * @param score score to set the fours to
     */
    public void setFours(int score) {
        fours = score;
    }

    /**
     * Sets the fives score to the given score.
     * @param score score to set the fives to.
     */
    public void setFives(int score) {
        fives = score;
    }

    /**
     * Sets the sixes score to the given score.
     * @param score score to set the sixes to.
     */
    public void setSixes(int score) {
        sixes = score;
    }

    /**
     * Sets three-of-a-kind score to the given score.
     * @param score score to set the field to.
     */
    public void setThreeOfAKind(int score) {
        threeOfAKind = score;
    }

    /**
     * Sets the four-of-a-kind score to the given score.
     * @param score score to set the field to.
     */
    public void setFourOfAKind(int score) {
        fourOfAKind = score;
    }

    /**
     * Sets the full house score, which is default 25 in yahtzee.
     */
    public void setFullHouse() {
        fullHouse = 25;
    }

    /**
     * Sets the small straight score, which is by default 30 in yahtzee.
     */
    public void setSmallStraight() {
        smallStraight = 30;
    }

    /**
     * Sets the Large straight score, which is by default 40 in yahtzee.
     */
    public void setLargeStraight() {
        largeStraight = 40;
    }

    /**
     * Sets the Yahtzee Score which is by default 50.
     */
    public void setYahtzee() {
        yahtzee = 50;
    }

    /**
     * Get the total score of the upper section. This consists of
     * The total of the ones, twos, threes, fours, fives, and sixes.
     * A bonus 35 points are awarded if the sum is 63 or more, which is
     * accounted for in this method.
     *
     * @return sum of the upper section of this scoreboard.
     */
    public int getUpperSectionTotal() {
        int sum = 0;
        if (ones != -1) sum += ones;
        if (twos != -1) sum += twos;
        if (threes != -1) sum += threes;
        if (fours != -1) sum += fours;
        if (fives != -1) sum += fives;
        if (sixes != -1) sum += sixes;

        if (sum >= 63) {
            sum += 35;
        }

        return sum;
    }


    /**
     * Get the total score of the lower section. This consists of scores
     * for the three & four of a kind, full house, yahtzee, and small & large
     * straights. A bonus of 100 points is added for each extra yahtzee (after the
     * first yahtzee) that a player has obtained, which is accounted for in this method
     * through the xCount variable.
     *
     * @return total score of lower section
     */
    public int getLowerSectionTotal() {
        int sum = 0;
        if (threeOfAKind != -1) sum += threeOfAKind;
        if (fourOfAKind != -1) sum += fourOfAKind;
        if (fullHouse != -1) sum += fullHouse;
        if (smallStraight != -1) sum += smallStraight;
        if (largeStraight != -1) sum += largeStraight;
        if (yahtzee != -1) sum += yahtzee;

        if (yahtzeeCount > 1) {
            sum += (yahtzeeCount - 1) * 100;
        }

        return sum;
    }


    /**
     * @return the total score of this scoreboard overall. The sum of lower and upper section.
     */
    public int getTotalScore() {
        return getUpperSectionTotal() + getLowerSectionTotal();
    }

}
