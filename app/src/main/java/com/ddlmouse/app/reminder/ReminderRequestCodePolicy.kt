package com.ddlmouse.app.reminder

object ReminderRequestCodePolicy {
    private const val SLOT_COUNT = 25

    fun forPlan(templateId: Long, planId: Long): Int {
        return (templateId * 31 + Math.floorMod(planId, SLOT_COUNT.toLong())).toInt()
    }

    fun cancelCodesForTemplate(templateId: Long): IntRange {
        val start = (templateId * 31).toInt()
        return start until start + SLOT_COUNT
    }
}
