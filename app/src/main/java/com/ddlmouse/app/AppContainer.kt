package com.ddlmouse.app

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    ).addMigrations(MIGRATION_1_2).build()

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

    private companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE task_templates ADD COLUMN note TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN repeatMode TEXT NOT NULL DEFAULT 'NONE'")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN reminderEnabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN preferredReminderMinuteOfDay INTEGER")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN timeBucket TEXT")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN weeklyDays TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN monthlyDay INTEGER")
                db.execSQL("ALTER TABLE task_templates ADD COLUMN projectStage TEXT")
            }
        }
    }
}
