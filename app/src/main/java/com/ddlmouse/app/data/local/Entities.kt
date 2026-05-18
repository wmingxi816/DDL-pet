package com.ddlmouse.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_templates")
data class TaskTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val module: String,
    val deadlineAt: Long?,
    val difficulty: String,
    val enabled: Boolean = true,
    val reminderOverrideAt: Long? = null
)

@Entity(tableName = "task_occurrences")
data class TaskOccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val title: String,
    val module: String,
    val periodKey: String,
    val deadlineAt: Long?,
    val difficulty: String,
    val status: String,
    val completedAt: Long?,
    val scoreAwarded: Int,
    val penaltyApplied: Int
)

@Entity(tableName = "reminder_plans")
data class ReminderPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val title: String,
    val triggerAt: Long,
    val deadlineAt: Long,
    val manual: Boolean,
    val delivered: Boolean = false
)

@Entity(tableName = "pet_state")
data class PetStateEntity(
    @PrimaryKey val id: Int = 1,
    val points: Int = 0,
    val level: Int = 1,
    val mood: Int = 70,
    val fullness: Int = 60,
    val equippedDress: String = "默认围巾",
    val unlockedDresses: String = "默认围巾",
    val unlockedExpressions: String = "普通|开心"
)

@Entity(tableName = "store_items")
data class StoreItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val price: Int,
    val purchased: Boolean = false
)

@Entity(tableName = "daily_summaries")
data class DailySummaryEntity(
    @PrimaryKey val businessDate: String,
    val completedTitles: String,
    val missedTitles: String,
    val upcomingDeadlineTitles: String,
    val pointsEarned: Int,
    val pointsLost: Int,
    val generatedAt: Long,
    val petLine: String,
    val shownAt: Long?
)

