package com.ddlmouse.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "ddl_mouse_settings")

class SettingsStore(private val context: Context) {
    private val notificationsEnabledKey = booleanPreferencesKey("notifications_enabled")

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[notificationsEnabledKey] ?: true }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[notificationsEnabledKey] = enabled
        }
    }
}

