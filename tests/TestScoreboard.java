import commands.yahtzee.Scoreboard;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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

    }

    @Test
    public void testUnfinishedBoard() {

    }

    @Test
    public void testBoardWithUpperBonus() {

    }

    @Test
    public void testBoardWithExtraYahtzees() {

    }

    @Test
    public void testBoardWithAllBonuses() {

    }
}
