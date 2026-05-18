package com.ddlmouse.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class ScheduleRecurrencePolicyTest {
    @Test
    fun weeklyTemplateOnlyGeneratesOnSelectedWeekdays() {
        val template = TaskTemplate(
            id = 11,
            title = "健身",
            module = TaskModule.WEEKLY,
            deadline = null,
            difficulty = Difficulty.MEDIUM,
            weeklyDays = setOf(1, 3)
        )

        assertEquals(
            "2026-W21-D1",
            SchedulePolicy.occurrencePeriodKey(template, LocalDateTime.of(2026, 5, 18, 8, 0))
        )
        assertNull(
            SchedulePolicy.occurrencePeriodKey(template, LocalDateTime.of(2026, 5, 19, 8, 0))
        )
        assertEquals(
            "2026-W21-D3",
            SchedulePolicy.occurrencePeriodKey(template, LocalDateTime.of(2026, 5, 20, 8, 0))
        )
    }

    @Test
    fun monthlyTemplateOnlyGeneratesOnSelectedMonthDay() {
        val template = TaskTemplate(
            id = 12,
            title = "月报",
            module = TaskModule.MONTHLY,
            deadline = null,
            difficulty = Difficulty.HARD,
            monthlyDay = 18
        )

        assertEquals(
            "2026-05-D18",
            SchedulePolicy.occurrencePeriodKey(template, LocalDateTime.of(2026, 5, 18, 8, 0))
        )
        assertNull(
            SchedulePolicy.occurrencePeriodKey(template, LocalDateTime.of(2026, 5, 19, 8, 0))
        )
    }

    @Test
    fun emptyWeeklyAndMonthlyRulesKeepOriginalCadence() {
        val weekly = TaskTemplate(
            id = 13,
            title = "周复盘",
            module = TaskModule.WEEKLY,
            deadline = null,
            difficulty = Difficulty.MEDIUM
        )
        val monthly = TaskTemplate(
            id = 14,
            title = "预算",
            module = TaskModule.MONTHLY,
            deadline = null,
            difficulty = Difficulty.HARD
        )
        val now = LocalDateTime.of(2026, 5, 18, 8, 0)

        assertEquals("2026-W21", SchedulePolicy.occurrencePeriodKey(weekly, now))
        assertEquals("2026-05", SchedulePolicy.occurrencePeriodKey(monthly, now))
    }
}
