package com.ddlmouse.app.domain

object TaskFormPolicy {
    fun fieldsFor(module: TaskModule): List<TaskFormField> {
        val base = listOf(
            TaskFormField.TITLE,
            TaskFormField.MODULE,
            TaskFormField.DIFFICULTY,
            TaskFormField.NOTE,
            TaskFormField.REMINDER_ENABLED
        )
        return base + when (module) {
            TaskModule.DAILY -> listOf(
                TaskFormField.TIME_BUCKET,
                TaskFormField.DAILY_REMINDER_TIME
            )
            TaskModule.WEEKLY -> listOf(
                TaskFormField.WEEKLY_DAYS,
                TaskFormField.WEEKLY_DEADLINE_TIME,
                TaskFormField.REMINDER_PREVIEW
            )
            TaskModule.MONTHLY -> listOf(
                TaskFormField.MONTHLY_DAY,
                TaskFormField.MONTHLY_DEADLINE_TIME,
                TaskFormField.REMINDER_PREVIEW
            )
            TaskModule.PROJECT -> listOf(
                TaskFormField.DEADLINE_REQUIRED,
                TaskFormField.PROJECT_STAGE,
                TaskFormField.REMINDER_PREVIEW
            )
            TaskModule.TODO -> listOf(
                TaskFormField.DEADLINE_OPTIONAL,
                TaskFormField.REMINDER_PREVIEW
            )
        }
    }

    fun repeatModeFor(module: TaskModule): RepeatMode = when (module) {
        TaskModule.DAILY -> RepeatMode.DAILY
        TaskModule.WEEKLY -> RepeatMode.WEEKLY
        TaskModule.MONTHLY -> RepeatMode.MONTHLY
        TaskModule.PROJECT,
        TaskModule.TODO -> RepeatMode.NONE
    }
}

object TaskSectionPolicy {
    fun summaries(occurrences: List<TaskOccurrence>): List<TaskSectionSummary> {
        return TaskModule.entries.map { module ->
            val moduleOccurrences = occurrences.filter { it.module == module }
            TaskSectionSummary(
                module = module,
                totalCount = moduleOccurrences.size,
                completedCount = moduleOccurrences.count { it.status == TaskStatus.COMPLETED },
                pendingCount = moduleOccurrences.count { it.status == TaskStatus.PENDING },
                missedCount = moduleOccurrences.count { it.status == TaskStatus.MISSED },
                pendingPoints = moduleOccurrences
                    .filter { it.status == TaskStatus.PENDING }
                    .sumOf { it.difficulty.points }
            )
        }
    }
}

