package com.ddlmouse.app.domain

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.max

object SchedulePolicy {
    fun businessDate(now: LocalDateTime): LocalDate {
        return if (now.toLocalTime().hour < 5) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
    }

    fun periodKey(module: TaskModule, now: LocalDateTime, templateId: Long): String {
        val date = businessDate(now)
        return when (module) {
            TaskModule.DAILY -> date.toString()
            TaskModule.WEEKLY -> {
                val weekFields = WeekFields.ISO
                val week = date.get(weekFields.weekOfWeekBasedYear())
                val year = date.get(weekFields.weekBasedYear())
                "%04d-W%02d".format(Locale.US, year, week)
            }
            TaskModule.MONTHLY -> "%04d-%02d".format(Locale.US, date.year, date.monthValue)
            TaskModule.PROJECT,
            TaskModule.TODO -> "single-$templateId"
        }
    }

    fun occurrencePeriodKey(template: TaskTemplate, now: LocalDateTime): String? {
        val date = businessDate(now)
        return when (template.module) {
            TaskModule.WEEKLY -> {
                if (template.weeklyDays.isEmpty()) {
                    periodKey(template.module, now, template.id)
                } else {
                    val dayOfWeek = date.dayOfWeek.value
                    if (dayOfWeek in template.weeklyDays) {
                        "${periodKey(template.module, now, template.id)}-D$dayOfWeek"
                    } else {
                        null
                    }
                }
            }
            TaskModule.MONTHLY -> {
                val selectedDay = template.monthlyDay
                if (selectedDay == null) {
                    periodKey(template.module, now, template.id)
                } else if (date.dayOfMonth == selectedDay) {
                    "${periodKey(template.module, now, template.id)}-D$selectedDay"
                } else {
                    null
                }
            }
            else -> periodKey(template.module, now, template.id)
        }
    }
}

object DifficultyPolicy {
    fun recommend(module: TaskModule, deadline: LocalDateTime?, now: LocalDateTime): Difficulty {
        val daysUntilDeadline = deadline?.let {
            ChronoUnit.DAYS.between(now.toLocalDate(), it.toLocalDate())
        }
        return when (module) {
            TaskModule.DAILY -> Difficulty.EASY
            TaskModule.WEEKLY -> Difficulty.MEDIUM
            TaskModule.MONTHLY -> Difficulty.HARD
            TaskModule.PROJECT,
            TaskModule.TODO -> if ((daysUntilDeadline ?: 0) > 14) Difficulty.HELL else Difficulty.HARD
        }
    }
}

object ReminderPolicy {
    fun defaultReminderTimes(now: LocalDateTime, deadline: LocalDateTime): List<LocalDateTime> {
        val duration = Duration.between(now, deadline)
        if (duration.isZero || duration.isNegative) return emptyList()

        val candidates = when {
            duration < Duration.ofHours(1) -> listOf(now.plusMinutes(10))
            duration < Duration.ofHours(6) -> listOf(deadline.minusHours(1))
            duration < Duration.ofHours(24) -> listOf(deadline.minusHours(2))
            duration <= Duration.ofDays(3) -> listOf(atEightPmDaysBefore(deadline, 1), deadline.minusHours(2))
            duration <= Duration.ofDays(7) -> listOf(
                atEightPmDaysBefore(deadline, 3),
                atEightPmDaysBefore(deadline, 1),
                deadline.minusHours(2)
            )
            duration <= Duration.ofDays(30) -> listOf(
                atEightPmDaysBefore(deadline, 7),
                atEightPmDaysBefore(deadline, 3),
                atEightPmDaysBefore(deadline, 1),
                deadline.minusHours(2)
            )
            else -> listOf(
                atEightPmDaysBefore(deadline, 30),
                atEightPmDaysBefore(deadline, 14),
                atEightPmDaysBefore(deadline, 7),
                atEightPmDaysBefore(deadline, 3),
                atEightPmDaysBefore(deadline, 1),
                deadline.minusHours(2)
            )
        }

        return candidates
            .filter { it.isAfter(now) && it.isBefore(deadline) }
            .distinct()
            .sorted()
    }

    private fun atEightPmDaysBefore(deadline: LocalDateTime, daysBefore: Long): LocalDateTime {
        return deadline.toLocalDate().minusDays(daysBefore).atTime(20, 0)
    }
}

object ScorePolicy {
    fun penaltyFor(difficulty: Difficulty): Int = max(3, difficulty.points / 2)

    fun applyDelta(current: Int, delta: Int): Int = max(0, current + delta)
}
