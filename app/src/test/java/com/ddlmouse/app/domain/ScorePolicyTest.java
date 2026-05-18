package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ScorePolicyTest {
    @Test
    public void missingTaskPenaltyIsHalfTheRewardWithMinimumThree() {
        assertEquals(3, ScorePolicy.INSTANCE.penaltyFor(Difficulty.EASY));
        assertEquals(6, ScorePolicy.INSTANCE.penaltyFor(Difficulty.MEDIUM));
        assertEquals(12, ScorePolicy.INSTANCE.penaltyFor(Difficulty.HARD));
        assertEquals(25, ScorePolicy.INSTANCE.penaltyFor(Difficulty.HELL));
    }

    @Test
    public void scoreNeverDropsBelowZero() {
        assertEquals(0, ScorePolicy.INSTANCE.applyDelta(2, -3));
        assertEquals(17, ScorePolicy.INSTANCE.applyDelta(12, 5));
    }
}

