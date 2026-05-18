package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;

import java.time.LocalDateTime;
import org.junit.Test;

public class DifficultyPolicyTest {
    @Test
    public void defaultDifficultyUsesModuleAndLongProjectDeadline() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 18, 8, 0);

        assertEquals(Difficulty.EASY, DifficultyPolicy.INSTANCE.recommend(TaskModule.DAILY, null, now));
        assertEquals(Difficulty.MEDIUM, DifficultyPolicy.INSTANCE.recommend(TaskModule.WEEKLY, null, now));
        assertEquals(Difficulty.HARD, DifficultyPolicy.INSTANCE.recommend(TaskModule.MONTHLY, null, now));
        assertEquals(Difficulty.HARD, DifficultyPolicy.INSTANCE.recommend(TaskModule.PROJECT, now.plusDays(10), now));
        assertEquals(Difficulty.HELL, DifficultyPolicy.INSTANCE.recommend(TaskModule.PROJECT, now.plusDays(20), now));
    }

    @Test
    public void difficultyScoresAreStable() {
        assertEquals(5, Difficulty.EASY.getPoints());
        assertEquals(12, Difficulty.MEDIUM.getPoints());
        assertEquals(25, Difficulty.HARD.getPoints());
        assertEquals(50, Difficulty.HELL.getPoints());
    }
}

