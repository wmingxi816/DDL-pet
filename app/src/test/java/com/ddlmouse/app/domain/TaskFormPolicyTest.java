package com.ddlmouse.app.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class TaskFormPolicyTest {
    @Test
    public void dailyTasksShowCycleFieldsButDoNotRequireDeadline() {
        List<TaskFormField> fields = TaskFormPolicy.INSTANCE.fieldsFor(TaskModule.DAILY);

        assertTrue(fields.contains(TaskFormField.TITLE));
        assertTrue(fields.contains(TaskFormField.TIME_BUCKET));
        assertTrue(fields.contains(TaskFormField.DAILY_REMINDER_TIME));
        assertTrue(fields.contains(TaskFormField.NOTE));
        assertFalse(fields.contains(TaskFormField.DEADLINE_REQUIRED));
        assertFalse(fields.contains(TaskFormField.PROJECT_STAGE));
    }

    @Test
    public void monthlyTasksShowMonthlyCycleFields() {
        List<TaskFormField> fields = TaskFormPolicy.INSTANCE.fieldsFor(TaskModule.MONTHLY);

        assertTrue(fields.contains(TaskFormField.MONTHLY_DAY));
        assertTrue(fields.contains(TaskFormField.MONTHLY_DEADLINE_TIME));
        assertTrue(fields.contains(TaskFormField.REMINDER_PREVIEW));
        assertFalse(fields.contains(TaskFormField.WEEKLY_DAYS));
    }

    @Test
    public void projectTasksRequireDeadlineAndAllowStage() {
        List<TaskFormField> fields = TaskFormPolicy.INSTANCE.fieldsFor(TaskModule.PROJECT);

        assertTrue(fields.contains(TaskFormField.DEADLINE_REQUIRED));
        assertTrue(fields.contains(TaskFormField.PROJECT_STAGE));
        assertTrue(fields.contains(TaskFormField.REMINDER_PREVIEW));
        assertFalse(fields.contains(TaskFormField.DAILY_REMINDER_TIME));
    }

    @Test
    public void repeatModeMatchesModule() {
        assertEquals(RepeatMode.DAILY, TaskFormPolicy.INSTANCE.repeatModeFor(TaskModule.DAILY));
        assertEquals(RepeatMode.WEEKLY, TaskFormPolicy.INSTANCE.repeatModeFor(TaskModule.WEEKLY));
        assertEquals(RepeatMode.MONTHLY, TaskFormPolicy.INSTANCE.repeatModeFor(TaskModule.MONTHLY));
        assertEquals(RepeatMode.NONE, TaskFormPolicy.INSTANCE.repeatModeFor(TaskModule.PROJECT));
        assertEquals(RepeatMode.NONE, TaskFormPolicy.INSTANCE.repeatModeFor(TaskModule.TODO));
    }
}

