package com.ddlmouse.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskHistoryPolicyTest {
    @Test
    fun groupsCompletedMissedAndArchivedOccurrences() {
        val occurrences = listOf(
            occurrence(1, TaskStatus.PENDING),
            occurrence(2, TaskStatus.COMPLETED),
            occurrence(3, TaskStatus.MISSED),
            occurrence(4, TaskStatus.ARCHIVED)
        )

        val groups = TaskHistoryPolicy.groups(occurrences)

        assertEquals(TaskStatus.COMPLETED, groups[0].status)
        assertEquals(listOf(2L), groups[0].occurrences.map { it.id })
        assertEquals(TaskStatus.MISSED, groups[1].status)
        assertEquals(listOf(3L), groups[1].occurrences.map { it.id })
        assertEquals(TaskStatus.ARCHIVED, groups[2].status)
        assertEquals(listOf(4L), groups[2].occurrences.map { it.id })
    }

    private fun occurrence(id: Long, status: TaskStatus): TaskOccurrence {
        return TaskOccurrence(
            id = id,
            templateId = id,
            title = "task-$id",
            module = TaskModule.TODO,
            periodKey = "single-$id",
            deadline = null,
            difficulty = Difficulty.EASY,
            status = status
        )
    }
}
