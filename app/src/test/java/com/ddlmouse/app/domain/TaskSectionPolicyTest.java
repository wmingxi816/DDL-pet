package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class TaskSectionPolicyTest {
    @Test
    public void sectionSummariesIncludeCountsAndPendingPointsForEveryModule() {
        List<TaskOccurrence> occurrences = Arrays.asList(
                occurrence(1, TaskModule.DAILY, Difficulty.EASY, TaskStatus.PENDING),
                occurrence(2, TaskModule.DAILY, Difficulty.MEDIUM, TaskStatus.COMPLETED),
                occurrence(3, TaskModule.PROJECT, Difficulty.HARD, TaskStatus.PENDING),
                occurrence(4, TaskModule.PROJECT, Difficulty.HELL, TaskStatus.MISSED));

        List<TaskSectionSummary> summaries = TaskSectionPolicy.INSTANCE.summaries(occurrences);

        TaskSectionSummary daily = summaries.get(0);
        assertEquals(TaskModule.DAILY, daily.getModule());
        assertEquals(2, daily.getTotalCount());
        assertEquals(1, daily.getCompletedCount());
        assertEquals(1, daily.getPendingCount());
        assertEquals(5, daily.getPendingPoints());

        TaskSectionSummary project = summaries.get(3);
        assertEquals(TaskModule.PROJECT, project.getModule());
        assertEquals(2, project.getTotalCount());
        assertEquals(0, project.getCompletedCount());
        assertEquals(1, project.getPendingCount());
        assertEquals(25, project.getPendingPoints());
    }

    private TaskOccurrence occurrence(long id, TaskModule module, Difficulty difficulty, TaskStatus status) {
        return new TaskOccurrence(
                id,
                id,
                "task-" + id,
                module,
                "period-" + id,
                null,
                difficulty,
                status,
                null,
                status == TaskStatus.COMPLETED ? difficulty.getPoints() : 0,
                status == TaskStatus.MISSED ? ScorePolicy.INSTANCE.penaltyFor(difficulty) : 0);
    }
}

