package com.gpsspy.gpstracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        val TRACKING_FREQUENCY_MS = longPreferencesKey("tracking_frequency_ms")
        const val DEFAULT_FREQUENCY_MS = 10000L // 10 seconds

        val HOME_LATITUDE = doublePreferencesKey("home_latitude")
        val HOME_LONGITUDE = doublePreferencesKey("home_longitude")
        val WORK_LATITUDE = doublePreferencesKey("work_latitude")
        val WORK_LONGITUDE = doublePreferencesKey("work_longitude")
        val ANALYSIS_RADIUS = floatPreferencesKey("analysis_radius")
        const val DEFAULT_ANALYSIS_RADIUS = 100f
    }

    val trackingFrequencyFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[TRACKING_FREQUENCY_MS] ?: DEFAULT_FREQUENCY_MS
        }

    val homeLocationFlow: Flow<Pair<Double?, Double?>> = context.dataStore.data
        .map { preferences ->
            Pair(preferences[HOME_LATITUDE], preferences[HOME_LONGITUDE])
        }

    val workLocationFlow: Flow<Pair<Double?, Double?>> = context.dataStore.data
        .map { preferences ->
            Pair(preferences[WORK_LATITUDE], preferences[WORK_LONGITUDE])
        }

    val analysisRadiusFlow: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[ANALYSIS_RADIUS] ?: DEFAULT_ANALYSIS_RADIUS
        }

    suspend fun updateTrackingFrequency(frequencyMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[TRACKING_FREQUENCY_MS] = frequencyMs
        }
    }

    suspend fun updateHomeLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[HOME_LATITUDE] = latitude
            preferences[HOME_LONGITUDE] = longitude
        }
    }

    suspend fun updateWorkLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { preferences ->
            preferences[WORK_LATITUDE] = latitude
            preferences[WORK_LONGITUDE] = longitude
        }
    }

    suspend fun updateAnalysisRadius(radius: Float) {
        context.dataStore.edit { preferences ->
            preferences[ANALYSIS_RADIUS] = radius
        }
    }
}

