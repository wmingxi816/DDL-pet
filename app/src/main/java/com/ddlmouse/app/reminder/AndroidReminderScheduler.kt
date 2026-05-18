package com.ddlmouse.app.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.ddlmouse.app.data.TimeMapper
import com.ddlmouse.app.domain.ReminderPlan

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(plan: ReminderPlan) {
        val triggerAtMillis = TimeMapper.requireEpochMillis(plan.triggerAt)
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val pendingIntent = pendingIntent(plan.id.toInt(), plan.templateId, plan.title)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    override fun cancelForTemplate(templateId: Long) {
        for (offset in 0..24) {
            val requestCode = (templateId * 31 + offset).toInt()
            alarmManager.cancel(pendingIntent(requestCode, templateId, ""))
        }
    }

    private fun pendingIntent(requestCode: Int, templateId: Long, title: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TEMPLATE_ID, templateId)
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

