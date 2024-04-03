import commands.yahtzee.Scoreboard;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestScoreboard {

    Scoreboard scoreboard = new Scoreboard("My scoreboard");

    @Test
    public void testNormalBoard() {
        scoreboard.setOnes(3);
        scoreboard.setTwos(0);
        scoreboard.setThrees(9);
        scoreboard.setFours(16);
        scoreboard.setFives(15);
        scoreboard.setSixes(6);
        scoreboard.setThreeOfAKind(26);
        scoreboard.setFourOfAKind(20);
        scoreboard.setFullHouse(0);
        scoreboard.setSmallStraight(30);
        scoreboard.setLargeStraight(40);
        scoreboard.setYahtzee(0);
        scoreboard.setChance(22);

        /* Ensure correct individual score fields */
        assertEquals(3, scoreboard.getOnes());
        assertEquals(0, scoreboard.getTwos());
        assertEquals(9, scoreboard.getThrees());
        assertEquals(16, scoreboard.getFours());
        assertEquals(15, scoreboard.getFives());
        assertEquals(6, scoreboard.getSixes());
        assertEquals(26, scoreboard.getThreeOfAKind());
        assertEquals(20, scoreboard.getFourOfAKind());
        assertEquals(0, scoreboard.getFullHouse());
        assertEquals(30, scoreboard.getSmallStraight());
        assertEquals(40, scoreboard.getLargeStraight());
        assertEquals(0, scoreboard.getYahtzee());
        assertEquals(22, scoreboard.getChance());

        /* Ensure correct bonuses */
        assertFalse(scoreboard.upperSectionHasBonus());
        assertEquals(0, scoreboard.getExtraYahtzeeCount());

        /* Ensure correct score totals */
        assertEquals(49, scoreboard.getUpperSectionTotal());
        assertEquals(49, scoreboard.getUpperSectionTotalWithoutBonus());
        assertEquals(138, scoreboard.getLowerSectionTotal());
        assertEquals(187, scoreboard.getTotalScore());
    }


    @Test
    public void testEmptyBoard() {
        scoreboard = new Scoreboard("Empty");

        assertEquals(-1, scoreboard.getOnes());
        assertEquals(-1, scoreboard.getTwos());
        assertEquals(-1, scoreboard.getThrees());
        assertEquals(-1, scoreboard.getFours());
        assertEquals(-1, scoreboard.getFives());
        assertEquals(-1, scoreboard.getSixes());
        assertEquals(-1, scoreboard.getThreeOfAKind());
        assertEquals(-1, scoreboard.getFourOfAKind());
        assertEquals(-1, scoreboard.getFullHouse());
        assertEquals(-1, scoreboard.getSmallStraight());
        assertEquals(-1, scoreboard.getLargeStraight());
        assertEquals(-1, scoreboard.getYahtzee());
        assertEquals(-1, scoreboard.getChance());

        assertFalse(scoreboard.upperSectionHasBonus());
        assertEquals(0, scoreboard.getExtraYahtzeeCount());

        assertEquals(0, scoreboard.getUpperSectionTotal());
        assertEquals(0, scoreboard.getUpperSectionTotalWithoutBonus());
        assertEquals(0, scoreboard.getLowerSectionTotal());
        assertEquals(0, scoreboard.getTotalScore());
    }

    @Test
    public void testUnfinishedBoard() {
        scoreboard.setOnes(0);
        scoreboard.setFives(20);
        scoreboard.setSixes(18);
        scoreboard.setThreeOfAKind(18);
        scoreboard.setFullHouse(25);
        scoreboard.setLargeStraight(0);
        scoreboard.setYahtzee(50);

        /* Ensure correct individual score fields */
        assertEquals(0, scoreboard.getOnes());
        assertEquals(-1, scoreboard.getTwos());
        assertEquals(-1, scoreboard.getThrees());
        assertEquals(-1, scoreboard.getFours());
        assertEquals(20, scoreboard.getFives());
        assertEquals(18, scoreboard.getSixes());
        assertEquals(18, scoreboard.getThreeOfAKind());
        assertEquals(-1, scoreboard.getFourOfAKind());
        assertEquals(25, scoreboard.getFullHouse());
        assertEquals(-1, scoreboard.getSmallStraight());
        assertEquals(0, scoreboard.getLargeStraight());
        assertEquals(50, scoreboard.getYahtzee());
        assertEquals(-1, scoreboard.getChance());

        /* Ensure correct bonuses */
        assertFalse(scoreboard.upperSectionHasBonus());
        assertEquals(0, scoreboard.getExtraYahtzeeCount());

        /* Ensure correct score totals */
        assertEquals(38, scoreboard.getUpperSectionTotal());
        assertEquals(38, scoreboard.getUpperSectionTotalWithoutBonus());
        assertEquals(93, scoreboard.getLowerSectionTotal());
        assertEquals(131, scoreboard.getTotalScore());
    }

    @Test
    public void testBoardWithUpperBonus() {
        scoreboard.setOnes(4);
        scoreboard.setTwos(8);
        scoreboard.setThrees(12);
        scoreboard.setFours(16);
        scoreboard.setFives(20);
        scoreboard.setSixes(24);
        scoreboard.setThreeOfAKind(10);
        scoreboard.setFourOfAKind(28);
        scoreboard.setFullHouse(0);
        scoreboard.setSmallStraight(0);
        scoreboard.setLargeStraight(40);
        scoreboard.setYahtzee(50);
        scoreboard.setChance(12);

        /* Ensure correct individual score fields */
        assertEquals(4, scoreboard.getOnes());
        assertEquals(8, scoreboard.getTwos());
        assertEquals(12, scoreboard.getThrees());
        assertEquals(16, scoreboard.getFours());
        assertEquals(20, scoreboard.getFives());
        assertEquals(24, scoreboard.getSixes());
        assertEquals(10, scoreboard.getThreeOfAKind());
        assertEquals(28, scoreboard.getFourOfAKind());
        assertEquals(0, scoreboard.getFullHouse());
        assertEquals(0, scoreboard.getSmallStraight());
        assertEquals(40, scoreboard.getLargeStraight());
        assertEquals(50, scoreboard.getYahtzee());
        assertEquals(12, scoreboard.getChance());

        /* Ensure correct bonuses */
        assertTrue(scoreboard.upperSectionHasBonus());
        assertEquals(0, scoreboard.getExtraYahtzeeCount());

        /* Ensure correct score totals */
        assertEquals(119, scoreboard.getUpperSectionTotal());
        assertEquals(84, scoreboard.getUpperSectionTotalWithoutBonus());
        assertEquals(140, scoreboard.getLowerSectionTotal());
        assertEquals(259, scoreboard.getTotalScore());
    }

    @Test
    public void testBoardWithExtraYahtzees() {

    }

    @Test
    public void testBoardWithAllBonuses() {

    }
}
