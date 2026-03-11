package com.example.gpstracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val TRACKING_FREQUENCY_MS = longPreferencesKey("tracking_frequency_ms")
        const val DEFAULT_FREQUENCY_MS = 10000L // 10 seconds
    }

    val trackingFrequencyFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[TRACKING_FREQUENCY_MS] ?: DEFAULT_FREQUENCY_MS
        }

    suspend fun updateTrackingFrequency(frequencyMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[TRACKING_FREQUENCY_MS] = frequencyMs
        }
    }
}
