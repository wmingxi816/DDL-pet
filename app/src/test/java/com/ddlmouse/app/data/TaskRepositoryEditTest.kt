package com.ddlmouse.app.data

import com.ddlmouse.app.data.local.ReminderPlanEntity
import com.ddlmouse.app.data.local.TaskDao
import com.ddlmouse.app.data.local.TaskOccurrenceEntity
import com.ddlmouse.app.data.local.TaskTemplateEntity
import com.ddlmouse.app.domain.DailySummary
import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.PetLineScene
import com.ddlmouse.app.domain.PetState
import com.ddlmouse.app.domain.ReminderPlan
import com.ddlmouse.app.domain.RepeatMode
import com.ddlmouse.app.domain.StoreItem
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskStatus
import com.ddlmouse.app.domain.TaskTemplate
import com.ddlmouse.app.reminder.ReminderScheduler
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskRepositoryEditTest {
    @Test
    fun updateTaskUpdatesTemplatePendingOccurrenceAndReminders() = runBlocking {
        val taskDao = FakeTaskDao()
        val oldDeadline = LocalDateTime.of(2026, 5, 20, 23, 0)
        taskDao.templates[42] = TaskTemplate(
            id = 42,
            title = "旧任务",
            module = TaskModule.TODO,
            deadline = oldDeadline,
            difficulty = Difficulty.EASY
        ).toEntity()
        taskDao.occurrences[5] = TaskOccurrence(
            id = 5,
            templateId = 42,
            title = "旧任务",
            module = TaskModule.TODO,
            periodKey = "42",
            deadline = oldDeadline,
            difficulty = Difficulty.EASY,
            status = TaskStatus.PENDING
        ).toEntity()
        val scheduler = RecordingReminderScheduler()
        val repository = DefaultTaskRepository(
            taskDao = taskDao,
            petRepository = NoopPetRepository(),
            dailySummaryRepository = NoopDailySummaryRepository(),
            reminderScheduler = scheduler
        )
        val newDeadline = LocalDateTime.of(2026, 5, 24, 21, 30)
        val reminder = LocalDateTime.of(2026, 5, 24, 9, 0)

        repository.updateTask(
            templateId = 42,
            title = "  交课程论文  ",
            module = TaskModule.PROJECT,
            deadline = newDeadline,
            difficulty = Difficulty.HARD,
            reminderOverride = reminder,
            note = "  初稿先行  ",
            repeatMode = RepeatMode.NONE,
            reminderEnabled = true,
            preferredReminderMinuteOfDay = 9 * 60,
            timeBucket = null,
            weeklyDays = emptySet(),
            monthlyDay = null,
            projectStage = "  初稿  "
        )

        val updatedTemplate = taskDao.templates.getValue(42).toDomain()
        assertEquals("交课程论文", updatedTemplate.title)
        assertEquals(TaskModule.PROJECT, updatedTemplate.module)
        assertEquals(newDeadline, updatedTemplate.deadline)
        assertEquals(Difficulty.HARD, updatedTemplate.difficulty)
        assertEquals("初稿先行", updatedTemplate.note)
        assertEquals("初稿", updatedTemplate.projectStage)

        val updatedOccurrence = taskDao.occurrences.getValue(5).toDomain()
        assertEquals("交课程论文", updatedOccurrence.title)
        assertEquals(TaskModule.PROJECT, updatedOccurrence.module)
        assertEquals(newDeadline, updatedOccurrence.deadline)
        assertEquals(Difficulty.HARD, updatedOccurrence.difficulty)
        assertEquals(TaskStatus.PENDING, updatedOccurrence.status)

        assertEquals(listOf(42L), taskDao.deletedReminderTemplateIds)
        assertEquals(listOf(42L), scheduler.cancelledTemplateIds)
        assertEquals(1, scheduler.scheduledPlans.size)
        assertEquals(reminder, scheduler.scheduledPlans.single().triggerAt)
    }
}

private class FakeTaskDao : TaskDao {
    val templates = mutableMapOf<Long, TaskTemplateEntity>()
    val occurrences = mutableMapOf<Long, TaskOccurrenceEntity>()
    val reminders = mutableMapOf<Long, ReminderPlanEntity>()
    val deletedReminderTemplateIds = mutableListOf<Long>()
    private var nextTemplateId = 100L
    private var nextOccurrenceId = 200L
    private var nextReminderId = 300L

    override fun observeTemplates(): Flow<List<TaskTemplateEntity>> = flowOf(templates.values.toList())

    override suspend fun activeTemplates(): List<TaskTemplateEntity> = templates.values.filter { it.enabled }

    override suspend fun templateById(id: Long): TaskTemplateEntity? = templates[id]

    override suspend fun insertTemplate(entity: TaskTemplateEntity): Long {
        val id = nextTemplateId++
        templates[id] = entity.copy(id = id)
        return id
    }

    override suspend fun updateTemplate(entity: TaskTemplateEntity) {
        templates[entity.id] = entity
    }

    override suspend fun disableTemplate(id: Long) {
        templates[id] = templates.getValue(id).copy(enabled = false)
    }

    override fun observeOccurrences(): Flow<List<TaskOccurrenceEntity>> = flowOf(occurrences.values.toList())

    override suspend fun allOccurrences(): List<TaskOccurrenceEntity> = occurrences.values.toList()

    override suspend fun occurrenceForTemplateAndPeriod(
        templateId: Long,
        periodKey: String
    ): TaskOccurrenceEntity? = occurrences.values.firstOrNull {
        it.templateId == templateId && it.periodKey == periodKey
    }

    override suspend fun occurrenceById(id: Long): TaskOccurrenceEntity? = occurrences[id]

    override suspend fun insertOccurrence(entity: TaskOccurrenceEntity): Long {
        val id = nextOccurrenceId++
        occurrences[id] = entity.copy(id = id)
        return id
    }

    override suspend fun updateOccurrence(entity: TaskOccurrenceEntity) {
        occurrences[entity.id] = entity
    }

    override suspend fun pendingOccurrencesForTemplate(templateId: Long): List<TaskOccurrenceEntity> {
        return occurrences.values.filter { it.templateId == templateId && it.status == TaskStatus.PENDING.name }
    }

    override suspend fun deleteRemindersForTemplate(templateId: Long) {
        deletedReminderTemplateIds += templateId
        reminders.entries.removeIf { it.value.templateId == templateId }
    }

    override suspend fun insertReminder(entity: ReminderPlanEntity): Long {
        val id = nextReminderId++
        reminders[id] = entity.copy(id = id)
        return id
    }

    override fun observeReminders(): Flow<List<ReminderPlanEntity>> = flowOf(reminders.values.toList())
}

private class RecordingReminderScheduler : ReminderScheduler {
    val scheduledPlans = mutableListOf<ReminderPlan>()
    val cancelledTemplateIds = mutableListOf<Long>()

    override fun schedule(plan: ReminderPlan) {
        scheduledPlans += plan
    }

    override fun cancelForTemplate(templateId: Long) {
        cancelledTemplateIds += templateId
    }
}

private class NoopPetRepository : PetRepository {
    override fun observePetState(): Flow<PetState> = flowOf(PetState())
    override fun observeStoreItems(): Flow<List<StoreItem>> = flowOf(emptyList())
    override suspend fun ensureSeedData() = Unit
    override suspend fun addPoints(points: Int) = Unit
    override suspend fun subtractPoints(points: Int) = Unit
    override suspend fun feed() = Unit
    override suspend fun buy(itemId: String) = Unit
    override fun randomLine(scene: PetLineScene): String = ""
}

private class NoopDailySummaryRepository : DailySummaryRepository {
    override fun observeUnshownSummary(): Flow<DailySummary?> = flowOf(null)
    override suspend fun generateIfNeeded(now: LocalDateTime, occurrences: List<TaskOccurrence>) = Unit
    override suspend fun markShown(summary: DailySummary, shownAt: LocalDateTime) = Unit
}
