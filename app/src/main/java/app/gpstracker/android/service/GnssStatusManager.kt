package com.gpsspy.gpstracker.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.GnssStatusCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GnssSatelliteInfo(
    val svid: Int,
    val constellationType: Int,
    val usedInFix: Boolean
)

class GnssStatusManager(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _satellites = MutableStateFlow<List<GnssSatelliteInfo>>(emptyList())
    val satellites: StateFlow<List<GnssSatelliteInfo>> = _satellites.asStateFlow()

    private var gnssCallback: GnssStatusCompat.Callback? = null

    fun startListening() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        gnssCallback = object : GnssStatusCompat.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatusCompat) {
                val satList = mutableListOf<GnssSatelliteInfo>()
                for (i in 0 until status.satelliteCount) {
                    satList.add(
                        GnssSatelliteInfo(
                            svid = status.getSvid(i),
                            constellationType = status.getConstellationType(i),
                            usedInFix = status.usedInFix(i)
                        )
                    )
                }
                _satellites.value = satList
            }
        }

        try {
            val executor = ContextCompat.getMainExecutor(context)
            LocationManagerCompat.registerGnssStatusCallback(locationManager, executor, gnssCallback!!)
        } catch (e: SecurityException) {
            Log.e("GnssStatusManager", "Permission denied for GNSS", e)
        }
    }

    fun stopListening() {
        if (gnssCallback != null) {
            LocationManagerCompat.unregisterGnssStatusCallback(locationManager, gnssCallback!!)
            gnssCallback = null
        }
        _satellites.value = emptyList()
    }
}

