package commands.yahtzee;

import java.util.*;

/**
 * This class represents a set of 5 dice used in Yahtzee.
 *
 * Includes operations on the dice such as rolling the dice to generate a
 * value between [1,6] and finding yahtzee patterns within rolled dice such as
 * straights, three of a kind, four-of-a-kind, etc.
 */
public class Dice {

    /* Five dice */
    private final int[] dice = new int[5];

    /* Minimum value on dice */
    public int MIN_DICE_VALUE = 1;

    /* Maximum value on dice */
    public int MAX_DICE_VALUE = 6;

    /**
     * An enum that represents the different accepted patterns that
     * Yahtzee dice could have.
     */
    private enum Pattern {
        NONE, /* None of the below patterns */
        THREE_OF_A_KIND, /* Three of the same dice */
        FOUR_OF_A_KIND, /* Four of the same dice */
        FULL_HOUSE, /* A pair, and a three-of-a-kind */
        LG_STRAIGHT, /* All 5 Dice in increasing order, 1 apart */
        SM_STRAIGHT, /* 4 dice in increasing order, 1 apart */
        YAHTZEE /* All five dice are the same */
    }

    /* The current pattern that these yahtzee dice have */
    private Set<Pattern> patterns;

    /**
     * Initialize the dice to all 1's
     */
    public Dice() {
        for (int i = 0; i < dice.length; i++) {
            dice[i] = 1;
        }
        patterns = new HashSet<>();
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
        findPattern();
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

    /**
     * Get the sum of all dice that are equal to value
     * @param value dice value, sum only dice with this value
     * @return sum of all dice that have value
     */
    public int getSum(int value) {
        int sum = 0;
        for (int i = 0; i < dice.length; i++) {
            if (dice[i] == value) {
                sum += dice[i];
            }
        }
        return sum;
    }

    /**
     * @return true if these dice contains three of the same dice
     */
    public boolean isThreeOfAKind() {
        return patterns.contains(Pattern.THREE_OF_A_KIND);
    }

    /**
     * @return true if these dice contains four of the same dice
     */
    public boolean isFourOfAKind() {
        return patterns.contains(Pattern.FOUR_OF_A_KIND);
    }

    /**
     * @return true if these dice contains a pair of the same dice, and three of the same dice
     */
    public boolean isFullHouse() {
        return patterns.contains(Pattern.FULL_HOUSE);
    }

    /**
     * @return true if four dice are in ascending order by one
     */
    public boolean isSmallStraight() {
        return patterns.contains(Pattern.SM_STRAIGHT);
    }

    /**
     * @return true if all five dice are in ascending order by one
     */
    public boolean isLargeStraight() {
        return patterns.contains(Pattern.LG_STRAIGHT);
    }

    /**
     * @return true if all five dice are the same value
     */
    public boolean isYahtzee() {
        return patterns.contains(Pattern.YAHTZEE);
    }


    /**
     * Based off of the current values of the dice,
     * determines all possible patterns on the dice.
     */
    private void findPattern() {
        patterns.clear();

        Map<Integer, Integer> valueToCount = getValueToFrequency();

        /* Determine of-a-kind patterns based on value to frequency mapping */
        for (Integer frequency : valueToCount.values()) {

            if (frequency >= 3) {
                patterns.add(Pattern.THREE_OF_A_KIND);
            }
            if (frequency >= 4) {
                patterns.add(Pattern.FOUR_OF_A_KIND);
            }
            if (frequency == 5) {
                patterns.add(Pattern.YAHTZEE);
                patterns.add(Pattern.FULL_HOUSE);
            }

        }

        /* Determine if Full House */
        if (valueToCount.containsValue(2) && valueToCount.containsValue(3)) {
            patterns.add(Pattern.FULL_HOUSE);
        }

        /* Determine if any straights */
        int straightCount = 1;
        int maxStraightCount = 1;
        for (int i = 1; i <= 5; i++) {
            int frequency = valueToCount.get(i);
            int nextFrequency = valueToCount.get(i + 1);
            if (frequency != 0 && nextFrequency != 0) {
                straightCount++;
            }
            else {
                if (straightCount > maxStraightCount) {
                    maxStraightCount = straightCount;
                }
                straightCount = 1;
            }
        }
        if (straightCount >= 4 || maxStraightCount >= 4) {
            patterns.add(Pattern.SM_STRAIGHT);
        }
        if (straightCount == 5 || maxStraightCount == 5) {
            patterns.add(Pattern.LG_STRAIGHT);
        }

    }


    /**
     * Find how many of each value there is and store it in valueToCount
     * e.g. if the dice values are 2, 4, 4, 4, 1 then the map will be
     * 1 -> value: 1
     * 2 -> value: 1
     * 3 -> value: 0
     * 4 -> value: 3
     * 5 -> value: 0
     * 6 -> value: 0
     */
    private Map<Integer, Integer> getValueToFrequency() {
        Map<Integer, Integer> valueToCount = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            valueToCount.put(i, 0);
        }
        for (int die : dice) {
            int plusOne = valueToCount.get(die) + 1;
            valueToCount.replace(die, plusOne);
        }

        return valueToCount;
    }

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
     * Sets the dice to have the new values. Not used in Yahtzee but
     * is helpful for testing due to the random nature of rolling the
     * dice.
     * @param values array of ints representing the new values of the dice
     */
    public void setValues(int[] values) {
        for (int i = 0; i < dice.length; i++) {
            dice[i] = values[i];
        }
        findPattern();
    }
}
