
import commands.yahtzee.Dice;
import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Class of test cases to test the Yahtzee dice.
 * Tests correct output sums of dice, and correct determination of
 * any patterns found within the dice e.g. three of a kind
 */
public class TestDice {
    commands.yahtzee.Dice dice = new Dice();

    @Test
    public void testSum() {
        int[] vals = {1,2,3,4,5};
        dice.setValues(vals);
        assertEquals(15, dice.getSum());
        assertEquals(1, dice.getSum(1));
        assertEquals(2, dice.getSum(2));
        assertEquals(3, dice.getSum(3));
        assertEquals(4, dice.getSum(4));
        assertEquals(5, dice.getSum(5));
        assertEquals(0, dice.getSum(6));

        dice.setValues(new int[]{1,1,1,1,1});
        assertEquals(5, dice.getSum());
        assertEquals(5, dice.getSum(1));
        assertEquals(0, dice.getSum(2));
        assertEquals(0, dice.getSum(3));
        assertEquals(0, dice.getSum(4));
        assertEquals(0, dice.getSum(5));
        assertEquals(0, dice.getSum(6));

        dice.setValues(new int[]{6,6,6,6,6});
        assertEquals(30, dice.getSum());
        assertEquals(0, dice.getSum(1));
        assertEquals(0, dice.getSum(2));
        assertEquals(0, dice.getSum(3));
        assertEquals(0, dice.getSum(4));
        assertEquals(0, dice.getSum(5));
        assertEquals(30, dice.getSum(6));
    }


    @Test
    public void testStraights() {

        /* Case: Large Straight Reverse */
        dice.setValues(new int[]{2,3,4,5,6});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertTrue(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        /* Case: Large straight Normal */
        dice.setValues(new int[]{1,2,3,4,5});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertTrue(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        /* Case: Small straight, not a large */
        dice.setValues(new int[]{2,2,3,4,5});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        /* Case: Small Straight, but not a large */
        dice.setValues(new int[]{5,6,3,4,1});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        /* Case: No Pattern */
        dice.setValues(new int[]{1,2,4,5,6});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());
    }


    @Test
    public void testOfAKind() {

        /* Case: No patterns */
        dice.setValues(new int[]{2,4,5,1,2});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());

        /* Case: 3 of a kind */
        dice.setValues(new int[]{1,4,6,1,1});
        assertFalse(dice.isFourOfAKind());
        assertTrue(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());

        /* Case: Full House */
        dice.setValues(new int[]{3,3,3,5,5});
        assertFalse(dice.isFourOfAKind());
        assertTrue(dice.isThreeOfAKind());
        assertTrue(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());

        /* Case: Yahtzee */
        dice.setValues(new int[]{4,4,4,4,4});
        assertTrue(dice.isFourOfAKind());
        assertTrue(dice.isThreeOfAKind());
        assertTrue(dice.isFullHouse());
        assertTrue(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());

        /* Case: 4 of a kind */
        dice.setValues(new int[]{2,5,2,2,2});
        assertTrue(dice.isFourOfAKind());
        assertTrue(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());
    }
}