package com.ddlmouse.app.reminder

import com.ddlmouse.app.domain.ReminderPlan

interface ReminderScheduler {
    fun schedule(plan: ReminderPlan)
    fun cancelForTemplate(templateId: Long)
}

