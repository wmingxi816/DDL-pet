package com.ddlmouse.app

import android.content.Context
import androidx.room.Room
import com.ddlmouse.app.data.DefaultDailySummaryRepository
import com.ddlmouse.app.data.DefaultPetRepository
import com.ddlmouse.app.data.DefaultTaskRepository
import com.ddlmouse.app.data.settings.SettingsStore
import com.ddlmouse.app.data.local.AppDatabase
import com.ddlmouse.app.reminder.AndroidReminderScheduler

class AppContainer(context: Context) {
    private val database: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "ddl_mouse.db"
    ).build()

    val settingsStore = SettingsStore(context)
    val petRepository = DefaultPetRepository(database.petDao())
    val dailySummaryRepository = DefaultDailySummaryRepository(database.dailySummaryDao())
    val reminderScheduler = AndroidReminderScheduler(context)
    val taskRepository = DefaultTaskRepository(
        taskDao = database.taskDao(),
        petRepository = petRepository,
        dailySummaryRepository = dailySummaryRepository,
        reminderScheduler = reminderScheduler
    )
}

