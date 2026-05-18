package com.ddlmouse.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        TaskTemplateEntity::class,
        TaskOccurrenceEntity::class,
        ReminderPlanEntity::class,
        PetStateEntity::class,
        StoreItemEntity::class,
        DailySummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun petDao(): PetDao
    abstract fun dailySummaryDao(): DailySummaryDao
}
