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
    private int chance = -1;

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
     * Sets the full house score, which is default 25, or 0 in yahtzee.
     * @param score score to set this field to.
     */
    public void setFullHouse(int score) {
        fullHouse = score;
    }

    /**
     * Sets the small straight score, which is by default 30, or 0 in yahtzee.
     * @param score score to set this field to.
     */
    public void setSmallStraight(int score) {
        smallStraight = score;
    }

    /**
     * Sets the Large straight score, which is by default 40, or 0 in yahtzee.
     * @param score score to set this field to.
     */
    public void setLargeStraight(int score) {
        largeStraight = score;
    }

    /**
     * Sets the Yahtzee Score which is by default 50, or 0.
     * @param score score to set this field to.
     */
    public void setYahtzee(int score) {
        yahtzee = score;
        yahtzeeCount++;
    }

    /**
     * Sets the chance score to the given score.
     * @param score score to set the field to.
     */
    public void setChance(int score) {
        chance = score;
    }

    /**
     * @return score for the ones.
     */
    public int getOnes() {
        return ones;
    }

    /**
     * @return score for the twos.
     */
    public int getTwos() {
        return twos;
    }

    /**
     * @return score for the threes.
     */
    public int getThrees() {
        return threes;
    }

    /**
     * @return score for the fours.
     */
    public int getFours() {
        return fours;
    }

    /**
     * @return score for the fives.
     */
    public int getFives() {
        return fives;
    }

    /**
     * @return score for the sixes.
     */
    public int getSixes() {
        return sixes;
    }

    /**
     * @return score for three of a kind
     */
    public int getThreeOfAKind() {
        return threeOfAKind;
    }

    /**
     * @return score for four of a kind
     */
    public int getFourOfAKind() {
        return fourOfAKind;
    }

    /**
     * @return score for Full House
     */
    public int getFullHouse() {
        return fullHouse;
    }

    /**
     * @return score for Small Straight
     */
    public int getSmallStraight() {
        return smallStraight;
    }

    /**
     * @return score for Large Straight
     */
    public int getLargeStraight() {
        return largeStraight;
    }

    /**
     * @return score for yahtzee
     */
    public int getYahtzee() {
        return yahtzee;
    }

    /**
     * @return score for chance
     */
    public int getChance() {
        return chance;
    }


    /**
     * Get the total score of the upper section. This consists of
     * The total of the ones, twos, threes, fours, fives, and sixes.
     * This does not account for the bonus.
     *
     * @return sum of the upper section of this scoreboard without bonus.
     */
    public int getUpperSectionTotalWithoutBonus() {
        int sum = 0;
        if (ones != -1) sum += ones;
        if (twos != -1) sum += twos;
        if (threes != -1) sum += threes;
        if (fours != -1) sum += fours;
        if (fives != -1) sum += fives;
        if (sixes != -1) sum += sixes;

        return sum;
    }

    /**
     * Whether the upper section of this scoreboard is eligible for
     * the 35 bonus points i.e. if raw upper section score is greater than or
     * equal to 63.
     *
     * @return true if bonus is eligible, false if not.
     */
    public boolean upperSectionHasBonus() {
        if (getUpperSectionTotalWithoutBonus() >= 63) {
            return true;
        }
        return false;
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
        int sum = getUpperSectionTotalWithoutBonus();

        if (upperSectionHasBonus()) {
            sum += 35;
        }

        return sum;
    }

    public int getExtraYahtzeeCount() {
        if (yahtzeeCount == 0) {
            return 0;
        }
        return yahtzeeCount - 1;
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
        if (chance != -1) sum += chance;

        sum += getExtraYahtzeeCount() * 100;

        return sum;
    }


    /**
     * @return the total score of this scoreboard overall. The sum of lower and upper section.
     */
    public int getTotalScore() {
        return getUpperSectionTotal() + getLowerSectionTotal();
    }

}
