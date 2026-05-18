package com.ddlmouse.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM task_templates WHERE enabled = 1 ORDER BY id DESC")
    fun observeTemplates(): Flow<List<TaskTemplateEntity>>

    @Query("SELECT * FROM task_templates WHERE enabled = 1 ORDER BY id DESC")
    suspend fun activeTemplates(): List<TaskTemplateEntity>

    @Query("SELECT * FROM task_templates WHERE id = :id LIMIT 1")
    suspend fun templateById(id: Long): TaskTemplateEntity?

    @Insert
    suspend fun insertTemplate(entity: TaskTemplateEntity): Long

    @Update
    suspend fun updateTemplate(entity: TaskTemplateEntity)

    @Query("UPDATE task_templates SET enabled = 0 WHERE id = :id")
    suspend fun disableTemplate(id: Long)

    @Query("SELECT * FROM task_occurrences ORDER BY CASE status WHEN 'PENDING' THEN 0 WHEN 'COMPLETED' THEN 1 ELSE 2 END, deadlineAt IS NULL, deadlineAt ASC, id DESC")
    fun observeOccurrences(): Flow<List<TaskOccurrenceEntity>>

    @Query("SELECT * FROM task_occurrences")
    suspend fun allOccurrences(): List<TaskOccurrenceEntity>

    @Query("SELECT * FROM task_occurrences WHERE templateId = :templateId AND periodKey = :periodKey LIMIT 1")
    suspend fun occurrenceForTemplateAndPeriod(templateId: Long, periodKey: String): TaskOccurrenceEntity?

    @Query("SELECT * FROM task_occurrences WHERE id = :id LIMIT 1")
    suspend fun occurrenceById(id: Long): TaskOccurrenceEntity?

    @Insert
    suspend fun insertOccurrence(entity: TaskOccurrenceEntity): Long

    @Update
    suspend fun updateOccurrence(entity: TaskOccurrenceEntity)

    @Query("DELETE FROM reminder_plans WHERE templateId = :templateId")
    suspend fun deleteRemindersForTemplate(templateId: Long)

    @Insert
    suspend fun insertReminder(entity: ReminderPlanEntity): Long

    @Query("SELECT * FROM reminder_plans WHERE delivered = 0 ORDER BY triggerAt ASC")
    fun observeReminders(): Flow<List<ReminderPlanEntity>>
}

@Dao
interface PetDao {
    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    fun observePetState(): Flow<PetStateEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1 LIMIT 1")
    suspend fun petState(): PetStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPetState(entity: PetStateEntity)

    @Query("SELECT * FROM store_items ORDER BY category, price")
    fun observeStoreItems(): Flow<List<StoreItemEntity>>

    @Query("SELECT * FROM store_items")
    suspend fun storeItems(): List<StoreItemEntity>

    @Query("SELECT * FROM store_items WHERE id = :id LIMIT 1")
    suspend fun storeItem(id: String): StoreItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStoreItems(items: List<StoreItemEntity>)

    @Update
    suspend fun updateStoreItem(item: StoreItemEntity)
}

@Dao
interface DailySummaryDao {
    @Query("SELECT * FROM daily_summaries WHERE businessDate = :businessDate LIMIT 1")
    suspend fun summaryForDate(businessDate: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summaries WHERE shownAt IS NULL ORDER BY generatedAt DESC LIMIT 1")
    fun observeUnshownSummary(): Flow<DailySummaryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSummary(entity: DailySummaryEntity)

    @Query("UPDATE daily_summaries SET shownAt = :shownAt WHERE businessDate = :businessDate")
    suspend fun markShown(businessDate: String, shownAt: Long)
}

