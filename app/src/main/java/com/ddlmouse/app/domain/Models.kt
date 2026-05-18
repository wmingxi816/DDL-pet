package com.ddlmouse.app.domain

import java.time.LocalDate
import java.time.LocalDateTime

enum class TaskModule(val label: String) {
    DAILY("每日任务"),
    WEEKLY("每周任务"),
    MONTHLY("每月任务"),
    PROJECT("大项目"),
    TODO("待办事件")
}

enum class Difficulty(val label: String, val points: Int) {
    EASY("简单", 5),
    MEDIUM("中等", 12),
    HARD("困难", 25),
    HELL("地狱", 50)
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    MISSED
}

data class TaskTemplate(
    val id: Long = 0,
    val title: String,
    val module: TaskModule,
    val deadline: LocalDateTime?,
    val difficulty: Difficulty,
    val enabled: Boolean = true,
    val reminderOverride: LocalDateTime? = null
)

data class TaskOccurrence(
    val id: Long = 0,
    val templateId: Long,
    val title: String,
    val module: TaskModule,
    val periodKey: String,
    val deadline: LocalDateTime?,
    val difficulty: Difficulty,
    val status: TaskStatus = TaskStatus.PENDING,
    val completedAt: LocalDateTime? = null,
    val scoreAwarded: Int = 0,
    val penaltyApplied: Int = 0
)

data class ReminderPlan(
    val id: Long = 0,
    val templateId: Long,
    val title: String,
    val triggerAt: LocalDateTime,
    val deadlineAt: LocalDateTime,
    val manual: Boolean = false,
    val delivered: Boolean = false
)

data class PetState(
    val points: Int = 0,
    val level: Int = 1,
    val mood: Int = 70,
    val fullness: Int = 60,
    val equippedDress: String = "默认围巾",
    val unlockedDresses: Set<String> = setOf("默认围巾"),
    val unlockedExpressions: Set<String> = setOf("普通", "开心")
)

enum class StoreCategory(val label: String) {
    FOOD("食物"),
    DRESS("装扮"),
    EXPRESSION("表情")
}

data class StoreItem(
    val id: String,
    val title: String,
    val category: StoreCategory,
    val price: Int,
    val purchased: Boolean = false
)

data class DailySummary(
    val businessDate: LocalDate,
    val completedTitles: List<String>,
    val missedTitles: List<String>,
    val upcomingDeadlineTitles: List<String>,
    val pointsEarned: Int,
    val pointsLost: Int,
    val generatedAt: LocalDateTime,
    val petLine: String,
    val shownAt: LocalDateTime? = null
)

