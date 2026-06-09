package com.example.data

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "app_settings")

class AppSettings(private val context: Context) {
    companion object {
        val KEY_SPEECH_ENABLED = booleanPreferencesKey("speech_enabled")
        val KEY_THEME_INDEX = intPreferencesKey("theme_index") // 0 = Dark, 1 = Light, 2 = Military Neon
    }

    val speechEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SPEECH_ENABLED] ?: true
    }

    val themeIndex: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_INDEX] ?: 0
    }

    suspend fun setSpeechEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SPEECH_ENABLED] = enabled
        }
    }

    suspend fun setThemeIndex(index: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_INDEX] = index
        }
    }
}

val LocalAppSettings = staticCompositionLocalOf<AppSettings> {
    error("No AppSettings provided")
}
