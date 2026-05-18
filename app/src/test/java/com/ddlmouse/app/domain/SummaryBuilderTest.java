package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.Test;

public class SummaryBuilderTest {
    @Test
    public void summaryGroupsCompletedMissedAndUpcomingTasks() {
        LocalDate businessDate = LocalDate.of(2026, 5, 17);
        TaskOccurrence completed = new TaskOccurrence(
                1,
                1,
                "背单词",
                TaskModule.DAILY,
                "2026-05-17",
                null,
                Difficulty.EASY,
                TaskStatus.COMPLETED,
                LocalDateTime.of(2026, 5, 17, 22, 0),
                5,
                0);
        TaskOccurrence missed = new TaskOccurrence(
                2,
                1,
                "周报",
                TaskModule.WEEKLY,
                "2026-W20",
                null,
                Difficulty.MEDIUM,
                TaskStatus.MISSED,
                null,
                0,
                6);
        TaskOccurrence upcoming = new TaskOccurrence(
                3,
                1,
                "论文初稿",
                TaskModule.PROJECT,
                "single-1",
                LocalDateTime.of(2026, 5, 19, 23, 0),
                Difficulty.HARD,
                TaskStatus.PENDING,
                null,
                0,
                0);

        DailySummary summary = SummaryBuilder.INSTANCE.build(
                businessDate,
                Arrays.asList(completed, missed, upcoming),
                LocalDateTime.of(2026, 5, 18, 5, 5),
                "昨天很努力，今天也稳稳来。");

        assertEquals(Arrays.asList("背单词"), summary.getCompletedTitles());
        assertEquals(Arrays.asList("周报"), summary.getMissedTitles());
        assertEquals(Arrays.asList("论文初稿"), summary.getUpcomingDeadlineTitles());
        assertEquals(5, summary.getPointsEarned());
        assertEquals(6, summary.getPointsLost());
        assertTrue(summary.getPetLine().contains("稳稳"));
    }
}

