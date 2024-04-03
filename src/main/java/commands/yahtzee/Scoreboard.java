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

    /* Number of extra yahtzees obtained */
    private int xCount = 0;


    public Scoreboard(String name) {
        this.name = name;
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

        sum += (xCount * 100);

        return sum;
    }


    /**
     * @return the total score of this scoreboard overall. The sum of lower and upper section.
     */
    public int getTotalScore() {
        return getUpperSectionTotal() + getLowerSectionTotal();
    }

}
