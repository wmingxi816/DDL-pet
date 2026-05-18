package com.ddlmouse.app.data

import com.ddlmouse.app.data.local.DailySummaryDao
import com.ddlmouse.app.data.local.PetDao
import com.ddlmouse.app.data.local.StoreItemEntity
import com.ddlmouse.app.data.local.TaskDao
import com.ddlmouse.app.domain.DailySummary
import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.DifficultyPolicy
import com.ddlmouse.app.domain.PetLineScene
import com.ddlmouse.app.domain.PetLines
import com.ddlmouse.app.domain.PetState
import com.ddlmouse.app.domain.ReminderPlan
import com.ddlmouse.app.domain.ReminderPolicy
import com.ddlmouse.app.domain.RepeatMode
import com.ddlmouse.app.domain.SchedulePolicy
import com.ddlmouse.app.domain.ScorePolicy
import com.ddlmouse.app.domain.StoreCategory
import com.ddlmouse.app.domain.StoreItem
import com.ddlmouse.app.domain.SummaryBuilder
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskStatus
import com.ddlmouse.app.domain.TaskTemplate
import com.ddlmouse.app.domain.TaskFormPolicy
import com.ddlmouse.app.reminder.ReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TaskRepository {
    fun observeOccurrences(): Flow<List<TaskOccurrence>>
    fun observeTemplates(): Flow<List<TaskTemplate>>
    suspend fun initializeForOpen(now: LocalDateTime)
    suspend fun createTask(
        title: String,
        module: TaskModule,
        deadline: LocalDateTime?,
        difficulty: Difficulty?,
        reminderOverride: LocalDateTime?,
        note: String = "",
        repeatMode: RepeatMode = TaskFormPolicy.repeatModeFor(module),
        reminderEnabled: Boolean = true,
        preferredReminderMinuteOfDay: Int? = null,
        timeBucket: String? = null,
        weeklyDays: Set<Int> = emptySet(),
        monthlyDay: Int? = null,
        projectStage: String? = null
    )
    suspend fun completeOccurrence(occurrenceId: Long, completedAt: LocalDateTime)
    suspend fun deleteTask(templateId: Long)
}

interface PetRepository {
    fun observePetState(): Flow<PetState>
    fun observeStoreItems(): Flow<List<StoreItem>>
    suspend fun ensureSeedData()
    suspend fun addPoints(points: Int)
    suspend fun subtractPoints(points: Int)
    suspend fun feed()
    suspend fun buy(itemId: String)
    fun randomLine(scene: PetLineScene): String
}

interface DailySummaryRepository {
    fun observeUnshownSummary(): Flow<DailySummary?>
    suspend fun generateIfNeeded(now: LocalDateTime, occurrences: List<TaskOccurrence>)
    suspend fun markShown(summary: DailySummary, shownAt: LocalDateTime)
}

class DefaultTaskRepository(
    private val taskDao: TaskDao,
    private val petRepository: PetRepository,
    private val dailySummaryRepository: DailySummaryRepository,
    private val reminderScheduler: ReminderScheduler
) : TaskRepository {
    override fun observeOccurrences(): Flow<List<TaskOccurrence>> {
        return taskDao.observeOccurrences().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeTemplates(): Flow<List<TaskTemplate>> {
        return taskDao.observeTemplates().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun initializeForOpen(now: LocalDateTime) {
        petRepository.ensureSeedData()
        generateCurrentOccurrences(now)
        settleMissedTasks(now)
        dailySummaryRepository.generateIfNeeded(now, taskDao.allOccurrences().map { it.toDomain() })
    }

    override suspend fun createTask(
        title: String,
        module: TaskModule,
        deadline: LocalDateTime?,
        difficulty: Difficulty?,
        reminderOverride: LocalDateTime?,
        note: String,
        repeatMode: RepeatMode,
        reminderEnabled: Boolean,
        preferredReminderMinuteOfDay: Int?,
        timeBucket: String?,
        weeklyDays: Set<Int>,
        monthlyDay: Int?,
        projectStage: String?
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return
        val finalDifficulty = difficulty ?: DifficultyPolicy.recommend(module, deadline, LocalDateTime.now())
        val templateId = taskDao.insertTemplate(
            TaskTemplate(
                title = cleanTitle,
                module = module,
                deadline = deadline,
                difficulty = finalDifficulty,
                reminderOverride = reminderOverride,
                note = note.trim(),
                repeatMode = repeatMode,
                reminderEnabled = reminderEnabled,
                preferredReminderMinuteOfDay = preferredReminderMinuteOfDay,
                timeBucket = timeBucket?.trim()?.takeIf { it.isNotEmpty() },
                weeklyDays = weeklyDays,
                monthlyDay = monthlyDay,
                projectStage = projectStage?.trim()?.takeIf { it.isNotEmpty() }
            ).toEntity()
        )
        createOccurrenceIfMissing(
            template = requireNotNull(taskDao.templateById(templateId)).toDomain(),
            now = LocalDateTime.now()
        )
        rebuildReminders(templateId)
    }

    override suspend fun completeOccurrence(occurrenceId: Long, completedAt: LocalDateTime) {
        val occurrence = taskDao.occurrenceById(occurrenceId)?.toDomain() ?: return
        if (occurrence.status != TaskStatus.PENDING) return
        val updated = occurrence.copy(
            status = TaskStatus.COMPLETED,
            completedAt = completedAt,
            scoreAwarded = occurrence.difficulty.points,
            penaltyApplied = 0
        )
        taskDao.updateOccurrence(updated.toEntity())
        petRepository.addPoints(updated.scoreAwarded)
    }

    override suspend fun deleteTask(templateId: Long) {
        taskDao.disableTemplate(templateId)
        taskDao.deleteRemindersForTemplate(templateId)
        reminderScheduler.cancelForTemplate(templateId)
    }

    private suspend fun generateCurrentOccurrences(now: LocalDateTime) {
        taskDao.activeTemplates()
            .map { it.toDomain() }
            .forEach { template -> createOccurrenceIfMissing(template, now) }
    }

    private suspend fun createOccurrenceIfMissing(template: TaskTemplate, now: LocalDateTime) {
        val periodKey = SchedulePolicy.periodKey(template.module, now, template.id)
        if (taskDao.occurrenceForTemplateAndPeriod(template.id, periodKey) != null) return
        taskDao.insertOccurrence(
            TaskOccurrence(
                templateId = template.id,
                title = template.title,
                module = template.module,
                periodKey = periodKey,
                deadline = template.deadline,
                difficulty = template.difficulty
            ).toEntity()
        )
    }

    private suspend fun settleMissedTasks(now: LocalDateTime) {
        val previousBusinessDate = SchedulePolicy.businessDate(now).minusDays(1).toString()
        taskDao.allOccurrences()
            .map { it.toDomain() }
            .filter { it.status == TaskStatus.PENDING }
            .filter { occurrence ->
                (occurrence.module == TaskModule.DAILY && occurrence.periodKey == previousBusinessDate) ||
                    occurrence.deadline?.isBefore(now) == true
            }
            .forEach { occurrence ->
                val penalty = ScorePolicy.penaltyFor(occurrence.difficulty)
                taskDao.updateOccurrence(
                    occurrence.copy(status = TaskStatus.MISSED, penaltyApplied = penalty).toEntity()
                )
                petRepository.subtractPoints(penalty)
            }
    }

    private suspend fun rebuildReminders(templateId: Long) {
        val template = taskDao.templateById(templateId)?.toDomain() ?: return
        taskDao.deleteRemindersForTemplate(templateId)
        reminderScheduler.cancelForTemplate(templateId)
        if (!template.reminderEnabled) return
        val deadline = template.deadline ?: return
        val reminderTimes = template.reminderOverride?.let { listOf(it) }
            ?: ReminderPolicy.defaultReminderTimes(LocalDateTime.now(), deadline)
        reminderTimes.forEach { triggerAt ->
            val planId = taskDao.insertReminder(
                ReminderPlan(
                    templateId = template.id,
                    title = template.title,
                    triggerAt = triggerAt,
                    deadlineAt = deadline,
                    manual = template.reminderOverride != null
                ).toEntity()
            )
            val plan = ReminderPlan(
                id = planId,
                templateId = template.id,
                title = template.title,
                triggerAt = triggerAt,
                deadlineAt = deadline,
                manual = template.reminderOverride != null
            )
            reminderScheduler.schedule(plan)
        }
    }
}

class DefaultPetRepository(private val petDao: PetDao) : PetRepository {
    override fun observePetState(): Flow<PetState> {
        return petDao.observePetState().map { it?.toDomain() ?: PetState() }
    }

    override fun observeStoreItems(): Flow<List<StoreItem>> {
        return petDao.observeStoreItems().map { items -> items.map { it.toDomain() } }
    }

    override suspend fun ensureSeedData() {
        if (petDao.petState() == null) {
            petDao.upsertPetState(PetState().toEntity())
        }
        if (petDao.storeItems().isEmpty()) {
            petDao.upsertStoreItems(defaultStoreItems())
        }
    }

    override suspend fun addPoints(points: Int) {
        val current = petDao.petState()?.toDomain() ?: PetState()
        val nextPoints = ScorePolicy.applyDelta(current.points, points)
        petDao.upsertPetState(
            current.copy(
                points = nextPoints,
                level = 1 + nextPoints / 100,
                mood = (current.mood + 4).coerceAtMost(100)
            ).toEntity()
        )
    }

    override suspend fun subtractPoints(points: Int) {
        val current = petDao.petState()?.toDomain() ?: PetState()
        val nextPoints = ScorePolicy.applyDelta(current.points, -points)
        petDao.upsertPetState(
            current.copy(
                points = nextPoints,
                level = 1 + nextPoints / 100,
                mood = (current.mood - 8).coerceAtLeast(0)
            ).toEntity()
        )
    }

    override suspend fun feed() {
        val current = petDao.petState()?.toDomain() ?: PetState()
        if (current.points < 8) return
        val nextPoints = ScorePolicy.applyDelta(current.points, -8)
        petDao.upsertPetState(
            current.copy(
                points = nextPoints,
                fullness = (current.fullness + 20).coerceAtMost(100),
                mood = (current.mood + 8).coerceAtMost(100)
            ).toEntity()
        )
    }

    override suspend fun buy(itemId: String) {
        val item = petDao.storeItem(itemId)?.toDomain() ?: return
        if (item.purchased) return
        val current = petDao.petState()?.toDomain() ?: PetState()
        if (current.points < item.price) return
        val nextPoints = ScorePolicy.applyDelta(current.points, -item.price)
        val nextState = when (item.category) {
            StoreCategory.FOOD -> current.copy(points = nextPoints, fullness = (current.fullness + 35).coerceAtMost(100))
            StoreCategory.DRESS -> current.copy(
                points = nextPoints,
                equippedDress = item.title,
                unlockedDresses = current.unlockedDresses + item.title
            )
            StoreCategory.EXPRESSION -> current.copy(
                points = nextPoints,
                unlockedExpressions = current.unlockedExpressions + item.title
            )
        }
        petDao.upsertPetState(nextState.copy(level = 1 + nextState.points / 100).toEntity())
        petDao.updateStoreItem(item.copy(purchased = true).toEntity())
    }

    override fun randomLine(scene: PetLineScene): String = PetLines.random(scene)

    private fun defaultStoreItems(): List<StoreItemEntity> = listOf(
        StoreItem("food_seed", "瓜子小份", StoreCategory.FOOD, 8),
        StoreItem("food_plate", "能量餐盘", StoreCategory.FOOD, 18),
        StoreItem("dress_scarf", "番茄红围巾", StoreCategory.DRESS, 35),
        StoreItem("dress_leaf", "薄荷叶帽", StoreCategory.DRESS, 60),
        StoreItem("expr_focus", "认真脸", StoreCategory.EXPRESSION, 45),
        StoreItem("expr_proud", "骄傲脸", StoreCategory.EXPRESSION, 70)
    ).map { it.toEntity() }
}

class DefaultDailySummaryRepository(
    private val summaryDao: DailySummaryDao
) : DailySummaryRepository {
    override fun observeUnshownSummary(): Flow<DailySummary?> {
        return summaryDao.observeUnshownSummary().map { it?.toDomain() }
    }

    override suspend fun generateIfNeeded(now: LocalDateTime, occurrences: List<TaskOccurrence>) {
        val previousBusinessDate: LocalDate = SchedulePolicy.businessDate(now).minusDays(1)
        if (summaryDao.summaryForDate(previousBusinessDate.toString()) != null) return
        val previousDateOccurrences = occurrences.filter { occurrence ->
            occurrence.completedAt?.let { SchedulePolicy.businessDate(it) == previousBusinessDate } == true ||
                (occurrence.module == TaskModule.DAILY && occurrence.periodKey == previousBusinessDate.toString()) ||
                (occurrence.status == TaskStatus.MISSED && occurrence.deadline?.toLocalDate() == previousBusinessDate) ||
                (occurrence.status == TaskStatus.PENDING && occurrence.deadline?.toLocalDate()?.let { !it.isAfter(previousBusinessDate.plusDays(3)) } == true)
        }
        val summary = SummaryBuilder.build(
            businessDate = previousBusinessDate,
            occurrences = previousDateOccurrences,
            generatedAt = now,
            petLine = PetLines.random(PetLineScene.SUMMARY)
        )
        summaryDao.upsertSummary(summary.toEntity())
    }

    override suspend fun markShown(summary: DailySummary, shownAt: LocalDateTime) {
        summaryDao.markShown(summary.businessDate.toString(), TimeMapper.requireEpochMillis(shownAt))
    }
}
