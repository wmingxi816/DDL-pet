package com.ddlmouse.app.data

import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskRepositoryArchiveTest {
    @Test
    fun archiveOccurrenceMovesCompletedOrMissedTaskToArchived() = runBlocking {
        val taskDao = FakeTaskDao()
        taskDao.occurrences[8] = TaskOccurrence(
            id = 8,
            templateId = 42,
            title = "已完成任务",
            module = TaskModule.TODO,
            periodKey = "single-42",
            deadline = null,
            difficulty = Difficulty.EASY,
            status = TaskStatus.COMPLETED
        ).toEntity()
        val repository = DefaultTaskRepository(
            taskDao = taskDao,
            petRepository = NoopPetRepository(),
            dailySummaryRepository = NoopDailySummaryRepository(),
            reminderScheduler = RecordingReminderScheduler()
        )

        repository.archiveOccurrence(8)

        assertEquals(TaskStatus.ARCHIVED, taskDao.occurrences.getValue(8).toDomain().status)
    }
}
