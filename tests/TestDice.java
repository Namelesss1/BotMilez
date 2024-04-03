
import commands.yahtzee.Dice;
import org.junit.Test;

import static org.junit.Assert.*;

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
        dice.setValues(new int[]{2,3,4,5,6});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertTrue(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        dice.setValues(new int[]{1,2,3,4,5});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertTrue(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        dice.setValues(new int[]{2,2,3,4,5});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        dice.setValues(new int[]{5,6,3,4,1});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertTrue(dice.isSmallStraight());

        dice.setValues(new int[]{1,2,4,5,6});
        assertFalse(dice.isFourOfAKind());
        assertFalse(dice.isThreeOfAKind());
        assertFalse(dice.isFullHouse());
        assertFalse(dice.isYahtzee());
        assertFalse(dice.isLargeStraight());
        assertFalse(dice.isSmallStraight());
    }
}