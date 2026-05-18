package com.ddlmouse.app.data

import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskTemplate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryReminderEditTest {
    @Test
    fun addManualReminderStoresAndSchedulesSingleReminder() = runBlocking {
        val taskDao = FakeTaskDao()
        val deadline = LocalDateTime.of(2026, 5, 24, 21, 30)
        val triggerAt = LocalDateTime.of(2026, 5, 24, 9, 0)
        taskDao.templates[42] = TaskTemplate(
            id = 42,
            title = "交课程论文",
            module = TaskModule.PROJECT,
            deadline = deadline,
            difficulty = Difficulty.HARD
        ).toEntity()
        val scheduler = RecordingReminderScheduler()
        val repository = DefaultTaskRepository(
            taskDao = taskDao,
            petRepository = NoopPetRepository(),
            dailySummaryRepository = NoopDailySummaryRepository(),
            reminderScheduler = scheduler
        )

        repository.addManualReminder(42, triggerAt)

        val reminders = repository.observeReminders().first()
        assertEquals(1, reminders.size)
        assertEquals(42, reminders.single().templateId)
        assertEquals(triggerAt, reminders.single().triggerAt)
        assertEquals(deadline, reminders.single().deadlineAt)
        assertTrue(reminders.single().manual)
        assertEquals(triggerAt, scheduler.scheduledPlans.single().triggerAt)
    }

    @Test
    fun deleteReminderRemovesReminderAndReschedulesRemainingTemplateReminders() = runBlocking {
        val taskDao = FakeTaskDao()
        val deadline = LocalDateTime.of(2026, 5, 24, 21, 30)
        taskDao.templates[42] = TaskTemplate(
            id = 42,
            title = "交课程论文",
            module = TaskModule.PROJECT,
            deadline = deadline,
            difficulty = Difficulty.HARD
        ).toEntity()
        val firstId = taskDao.insertReminder(
            com.ddlmouse.app.domain.ReminderPlan(
                templateId = 42,
                title = "交课程论文",
                triggerAt = LocalDateTime.of(2026, 5, 24, 9, 0),
                deadlineAt = deadline,
                manual = true
            ).toEntity()
        )
        val secondTrigger = LocalDateTime.of(2026, 5, 24, 18, 0)
        taskDao.insertReminder(
            com.ddlmouse.app.domain.ReminderPlan(
                templateId = 42,
                title = "交课程论文",
                triggerAt = secondTrigger,
                deadlineAt = deadline,
                manual = true
            ).toEntity()
        )
        val scheduler = RecordingReminderScheduler()
        val repository = DefaultTaskRepository(
            taskDao = taskDao,
            petRepository = NoopPetRepository(),
            dailySummaryRepository = NoopDailySummaryRepository(),
            reminderScheduler = scheduler
        )

        repository.deleteReminder(firstId)

        val reminders = repository.observeReminders().first()
        assertEquals(1, reminders.size)
        assertEquals(secondTrigger, reminders.single().triggerAt)
        assertEquals(listOf(42L), scheduler.cancelledTemplateIds)
        assertEquals(secondTrigger, scheduler.scheduledPlans.single().triggerAt)
    }
}
