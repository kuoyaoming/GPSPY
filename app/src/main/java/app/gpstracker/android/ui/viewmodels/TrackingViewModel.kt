package com.gpsspy.gpstracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsspy.gpstracker.data.database.LocationDao
import com.gpsspy.gpstracker.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.gpsspy.gpstracker.service.LocationTrackingService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val locationDao: LocationDao,
    private val gnssStatusManager: com.gpsspy.gpstracker.service.GnssStatusManager
) : ViewModel() {

    // Observe tracking state directly from the Foreground Service
    val isTracking: StateFlow<Boolean> = LocationTrackingService.isTracking

    val currentLocation: StateFlow<android.location.Location?> = LocationTrackingService.currentLocation

    val satellites: StateFlow<List<com.gpsspy.gpstracker.service.GnssSatelliteInfo>> = gnssStatusManager.satellites

    val trackingFrequencyMs: StateFlow<Long> = settingsRepository.trackingFrequencyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsRepository.DEFAULT_FREQUENCY_MS
    )

    fun updateFrequency(newFreqMs: Long) {
        viewModelScope.launch {
            settingsRepository.updateTrackingFrequency(newFreqMs)
        }
    }

    suspend fun getLatestSessionId(): Long? {
        return locationDao.getLatestSessionId()
    }

    suspend fun getPointsForSession(sessionId: Long) = locationDao.getLocationPointsForSessionSync(sessionId)
}



