package com.ddlmouse.app.domain

import java.time.LocalDate
import java.time.LocalDateTime

object SummaryBuilder {
    fun build(
        businessDate: LocalDate,
        occurrences: List<TaskOccurrence>,
        generatedAt: LocalDateTime,
        petLine: String
    ): DailySummary {
        return DailySummary(
            businessDate = businessDate,
            completedTitles = occurrences
                .filter { it.status == TaskStatus.COMPLETED }
                .map { it.title },
            missedTitles = occurrences
                .filter { it.status == TaskStatus.MISSED }
                .map { it.title },
            upcomingDeadlineTitles = occurrences
                .filter { it.status == TaskStatus.PENDING && it.deadline != null }
                .sortedBy { it.deadline }
                .map { it.title },
            pointsEarned = occurrences.sumOf { it.scoreAwarded },
            pointsLost = occurrences.sumOf { it.penaltyApplied },
            generatedAt = generatedAt,
            petLine = petLine
        )
    }
}

