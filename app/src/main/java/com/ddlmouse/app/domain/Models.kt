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

enum class RepeatMode(val label: String) {
    NONE("不循环"),
    DAILY("每天"),
    WEEKLY("每周"),
    MONTHLY("每月")
}

enum class TaskFormField(val label: String) {
    TITLE("任务名称"),
    MODULE("任务类型"),
    DIFFICULTY("难度"),
    NOTE("备注"),
    REMINDER_ENABLED("开启提醒"),
    REMINDER_PREVIEW("提醒预览"),
    DEADLINE_OPTIONAL("DDL，可选"),
    DEADLINE_REQUIRED("DDL，建议填写"),
    TIME_BUCKET("打卡时间段"),
    DAILY_REMINDER_TIME("每日提醒时间"),
    WEEKLY_DAYS("每周出现日期"),
    WEEKLY_DEADLINE_TIME("本周截止时间"),
    MONTHLY_DAY("每月出现日期"),
    MONTHLY_DEADLINE_TIME("每月截止时间"),
    PROJECT_STAGE("项目阶段")
}

data class TaskTemplate(
    val id: Long = 0,
    val title: String,
    val module: TaskModule,
    val deadline: LocalDateTime?,
    val difficulty: Difficulty,
    val enabled: Boolean = true,
    val reminderOverride: LocalDateTime? = null,
    val note: String = "",
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val reminderEnabled: Boolean = true,
    val preferredReminderMinuteOfDay: Int? = null,
    val timeBucket: String? = null,
    val weeklyDays: Set<Int> = emptySet(),
    val monthlyDay: Int? = null,
    val projectStage: String? = null
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

data class TaskSectionSummary(
    val module: TaskModule,
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int,
    val missedCount: Int,
    val pendingPoints: Int
)

data class TaskEditDraft(
    val title: String,
    val module: TaskModule,
    val deadline: LocalDateTime?,
    val difficulty: Difficulty,
    val reminderOverride: LocalDateTime? = null,
    val note: String = "",
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val reminderEnabled: Boolean = true,
    val preferredReminderMinuteOfDay: Int? = null,
    val timeBucket: String? = null,
    val weeklyDays: Set<Int> = emptySet(),
    val monthlyDay: Int? = null,
    val projectStage: String? = null
)
