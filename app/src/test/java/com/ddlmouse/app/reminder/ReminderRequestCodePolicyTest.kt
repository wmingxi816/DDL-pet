package com.ddlmouse.app.reminder

import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderRequestCodePolicyTest {
    @Test
    fun reminderRequestCodeCanBeCancelledByTemplateSweep() {
        val requestCode = ReminderRequestCodePolicy.forPlan(templateId = 42, planId = 301)

        assertTrue(requestCode in ReminderRequestCodePolicy.cancelCodesForTemplate(42))
    }
}
