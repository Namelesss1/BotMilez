package commands.yahtzee;

import java.util.Arrays;
import java.util.Random;

/**
 * This class represents a set of 5 dice used in Yahtzee.
 */
public class Dice {

    /* Five dice */
    private final int[] dice = new int[5];

    /* Minimum value on dice */
    public int MIN_DICE_VALUE = 1;

    /* Maximum value on dice */
    public int MAX_DICE_VALUE = 6;

    /**
     * "Rolls" a specific die. I.e. Randomly generate number from 1-6
     * @param dieToRoll index of die to roll
     */
    public void roll(int dieToRoll) {
        Random random = new Random();
        dice[dieToRoll] = random.nextInt(MAX_DICE_VALUE - MIN_DICE_VALUE)
                + MIN_DICE_VALUE;
    }

    /**
     * "Rolls" multiple dice. I.e. Randomly generates numbers 1-6 for each die.
     * @param diceToRoll array of booleans, true if corresponding die should roll
     */
    public void roll(boolean[] diceToRoll) {
        for (int i = 0; i < dice.length; i++) {
            if (diceToRoll[i]) {
                roll(i);
            }
        }
    }


    /**
     * Return a copy of the dice and their current values
     * @return a copy of the dice and their current values
     */
    public int[] getDice() {
        return Arrays.copyOf(dice, dice.length);
    }


    /**
     * @return The sum of all 5 dice
     */
    public int getSum() {
        int sum = 0;
        for (int i = 0; i < dice.length; i++) {
            sum += dice[i];
        }
        return sum;
    }


    //private int
}
