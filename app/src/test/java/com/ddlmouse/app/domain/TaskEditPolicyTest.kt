package com.ddlmouse.app.domain

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TaskEditPolicyTest {
    @Test
    fun applyingDraftPreservesIdentityAndNormalizesEditableFields() {
        val original = TaskTemplate(
            id = 7,
            title = "旧任务",
            module = TaskModule.DAILY,
            deadline = null,
            difficulty = Difficulty.EASY,
            enabled = false,
            note = "旧备注",
            repeatMode = RepeatMode.DAILY,
            reminderEnabled = true,
            timeBucket = "早上"
        )
        val deadline = LocalDateTime.of(2026, 5, 23, 21, 30)
        val reminder = LocalDateTime.of(2026, 5, 23, 9, 0)

        val updated = TaskEditPolicy.apply(
            original = original,
            draft = TaskEditDraft(
                title = "  交课程论文  ",
                module = TaskModule.PROJECT,
                deadline = deadline,
                difficulty = Difficulty.HARD,
                reminderOverride = reminder,
                note = "  先完成大纲  ",
                repeatMode = RepeatMode.NONE,
                reminderEnabled = false,
                preferredReminderMinuteOfDay = 9 * 60,
                timeBucket = "  晚上  ",
                weeklyDays = setOf(1, 3, 9),
                monthlyDay = 40,
                projectStage = "  初稿  "
            )
        )

        assertEquals(7, updated.id)
        assertFalse(updated.enabled)
        assertEquals("交课程论文", updated.title)
        assertEquals(TaskModule.PROJECT, updated.module)
        assertEquals(deadline, updated.deadline)
        assertEquals(Difficulty.HARD, updated.difficulty)
        assertEquals(reminder, updated.reminderOverride)
        assertEquals("先完成大纲", updated.note)
        assertEquals(RepeatMode.NONE, updated.repeatMode)
        assertFalse(updated.reminderEnabled)
        assertEquals(9 * 60, updated.preferredReminderMinuteOfDay)
        assertEquals("晚上", updated.timeBucket)
        assertEquals(setOf(1, 3), updated.weeklyDays)
        assertEquals(null, updated.monthlyDay)
        assertEquals("初稿", updated.projectStage)
    }
}
