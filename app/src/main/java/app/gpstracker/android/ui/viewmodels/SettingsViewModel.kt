package com.gpsspy.gpstracker.ui.viewmodels

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsspy.gpstracker.data.preferences.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val homeLocation: StateFlow<Pair<Double?, Double?>> = settingsRepository.homeLocationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(null, null)
    )

    val workLocation: StateFlow<Pair<Double?, Double?>> = settingsRepository.workLocationFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Pair(null, null)
    )

    val analysisRadius: StateFlow<Float> = settingsRepository.analysisRadiusFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsRepository.DEFAULT_ANALYSIS_RADIUS
    )

    fun updateAnalysisRadius(radius: Float) {
        viewModelScope.launch {
            settingsRepository.updateAnalysisRadius(radius)
        }
    }

    fun geocodeAddress(addressStr: String, isHome: Boolean, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocationName(addressStr, 1, object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) {
                                handleGeocodeResult(addresses, isHome, onResult)
                            }
                            override fun onError(errorMessage: String?) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    onResult(false, errorMessage ?: "Geocoding failed")
                                }
                            }
                        })
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocationName(addressStr, 1)
                        handleGeocodeResult(addresses, isHome, onResult)
                    }
                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        onResult(false, e.message ?: "Network error during geocoding")
                    }
                }
            }
        }
    }

    private fun handleGeocodeResult(addresses: List<Address>?, isHome: Boolean, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val lat = location.latitude
                val lng = location.longitude
                if (isHome) {
                    settingsRepository.updateHomeLocation(lat, lng)
                } else {
                    settingsRepository.updateWorkLocation(lat, lng)
                }
                withContext(Dispatchers.Main) {
                    onResult(true, null)
                }
            } else {
                 withContext(Dispatchers.Main) {
                    onResult(false, "Address not found")
                }
            }
        }
    }
}
