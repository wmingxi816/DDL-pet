package com.ddlmouse.app.data

import com.ddlmouse.app.data.local.DailySummaryEntity
import com.ddlmouse.app.data.local.PetStateEntity
import com.ddlmouse.app.data.local.ReminderPlanEntity
import com.ddlmouse.app.data.local.StoreItemEntity
import com.ddlmouse.app.data.local.TaskOccurrenceEntity
import com.ddlmouse.app.data.local.TaskTemplateEntity
import com.ddlmouse.app.domain.DailySummary
import com.ddlmouse.app.domain.Difficulty
import com.ddlmouse.app.domain.PetState
import com.ddlmouse.app.domain.ReminderPlan
import com.ddlmouse.app.domain.StoreCategory
import com.ddlmouse.app.domain.StoreItem
import com.ddlmouse.app.domain.TaskModule
import com.ddlmouse.app.domain.TaskOccurrence
import com.ddlmouse.app.domain.TaskStatus
import com.ddlmouse.app.domain.TaskTemplate
import java.time.LocalDate

private const val LIST_SEPARATOR = "|"

fun TaskTemplateEntity.toDomain(): TaskTemplate = TaskTemplate(
    id = id,
    title = title,
    module = TaskModule.valueOf(module),
    deadline = TimeMapper.fromEpochMillis(deadlineAt),
    difficulty = Difficulty.valueOf(difficulty),
    enabled = enabled,
    reminderOverride = TimeMapper.fromEpochMillis(reminderOverrideAt)
)

fun TaskTemplate.toEntity(): TaskTemplateEntity = TaskTemplateEntity(
    id = id,
    title = title,
    module = module.name,
    deadlineAt = TimeMapper.toEpochMillis(deadline),
    difficulty = difficulty.name,
    enabled = enabled,
    reminderOverrideAt = TimeMapper.toEpochMillis(reminderOverride)
)

fun TaskOccurrenceEntity.toDomain(): TaskOccurrence = TaskOccurrence(
    id = id,
    templateId = templateId,
    title = title,
    module = TaskModule.valueOf(module),
    periodKey = periodKey,
    deadline = TimeMapper.fromEpochMillis(deadlineAt),
    difficulty = Difficulty.valueOf(difficulty),
    status = TaskStatus.valueOf(status),
    completedAt = TimeMapper.fromEpochMillis(completedAt),
    scoreAwarded = scoreAwarded,
    penaltyApplied = penaltyApplied
)

fun TaskOccurrence.toEntity(): TaskOccurrenceEntity = TaskOccurrenceEntity(
    id = id,
    templateId = templateId,
    title = title,
    module = module.name,
    periodKey = periodKey,
    deadlineAt = TimeMapper.toEpochMillis(deadline),
    difficulty = difficulty.name,
    status = status.name,
    completedAt = TimeMapper.toEpochMillis(completedAt),
    scoreAwarded = scoreAwarded,
    penaltyApplied = penaltyApplied
)

fun ReminderPlanEntity.toDomain(): ReminderPlan = ReminderPlan(
    id = id,
    templateId = templateId,
    title = title,
    triggerAt = requireNotNull(TimeMapper.fromEpochMillis(triggerAt)),
    deadlineAt = requireNotNull(TimeMapper.fromEpochMillis(deadlineAt)),
    manual = manual,
    delivered = delivered
)

fun ReminderPlan.toEntity(): ReminderPlanEntity = ReminderPlanEntity(
    id = id,
    templateId = templateId,
    title = title,
    triggerAt = TimeMapper.requireEpochMillis(triggerAt),
    deadlineAt = TimeMapper.requireEpochMillis(deadlineAt),
    manual = manual,
    delivered = delivered
)

fun PetStateEntity.toDomain(): PetState = PetState(
    points = points,
    level = level,
    mood = mood,
    fullness = fullness,
    equippedDress = equippedDress,
    unlockedDresses = splitList(unlockedDresses).toSet(),
    unlockedExpressions = splitList(unlockedExpressions).toSet()
)

fun PetState.toEntity(): PetStateEntity = PetStateEntity(
    points = points,
    level = level,
    mood = mood,
    fullness = fullness,
    equippedDress = equippedDress,
    unlockedDresses = unlockedDresses.joinToString(LIST_SEPARATOR),
    unlockedExpressions = unlockedExpressions.joinToString(LIST_SEPARATOR)
)

fun StoreItemEntity.toDomain(): StoreItem = StoreItem(
    id = id,
    title = title,
    category = StoreCategory.valueOf(category),
    price = price,
    purchased = purchased
)

fun StoreItem.toEntity(): StoreItemEntity = StoreItemEntity(
    id = id,
    title = title,
    category = category.name,
    price = price,
    purchased = purchased
)

fun DailySummaryEntity.toDomain(): DailySummary = DailySummary(
    businessDate = LocalDate.parse(businessDate),
    completedTitles = splitList(completedTitles),
    missedTitles = splitList(missedTitles),
    upcomingDeadlineTitles = splitList(upcomingDeadlineTitles),
    pointsEarned = pointsEarned,
    pointsLost = pointsLost,
    generatedAt = requireNotNull(TimeMapper.fromEpochMillis(generatedAt)),
    petLine = petLine,
    shownAt = TimeMapper.fromEpochMillis(shownAt)
)

fun DailySummary.toEntity(): DailySummaryEntity = DailySummaryEntity(
    businessDate = businessDate.toString(),
    completedTitles = completedTitles.joinToString(LIST_SEPARATOR),
    missedTitles = missedTitles.joinToString(LIST_SEPARATOR),
    upcomingDeadlineTitles = upcomingDeadlineTitles.joinToString(LIST_SEPARATOR),
    pointsEarned = pointsEarned,
    pointsLost = pointsLost,
    generatedAt = TimeMapper.requireEpochMillis(generatedAt),
    petLine = petLine,
    shownAt = TimeMapper.toEpochMillis(shownAt)
)

private fun splitList(value: String): List<String> {
    return value.split(LIST_SEPARATOR).filter { it.isNotBlank() }
}

