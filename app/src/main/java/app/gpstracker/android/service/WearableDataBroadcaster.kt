package com.gpsspy.gpstracker.service

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class WearableDataBroadcaster(private val context: Context) {

    private val dataClient = Wearable.getDataClient(context)
    private var scope = CoroutineScope(Dispatchers.IO + Job())

    companion object {
        private const val TAG = "WearableDataBroadcaster"
        const val PATH_LOCATION_DATA = "/location_data"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_ALTITUDE = "altitude"
        const val KEY_IS_TRACKING = "is_tracking"
        const val KEY_TIMESTAMP = "timestamp"
    }

    fun startBroadcasting() {
        scope.launch {
            LocationTrackingService.isTracking.collect { isTracking ->
                broadcastState(isTracking, LocationTrackingService.currentLocation.value)
            }
        }

        scope.launch {
            LocationTrackingService.currentLocation.collect { location ->
                if (location != null) {
                    broadcastState(LocationTrackingService.isTracking.value, location)
                }
            }
        }
    }

    fun stopBroadcasting() {
        scope.cancel()
    }

    private fun broadcastState(isTracking: Boolean, location: android.location.Location?) {
        val putDataMapReq = PutDataMapRequest.create(PATH_LOCATION_DATA)
        putDataMapReq.dataMap.putBoolean(KEY_IS_TRACKING, isTracking)
        putDataMapReq.dataMap.putLong(KEY_TIMESTAMP, System.currentTimeMillis())

        if (location != null) {
            putDataMapReq.dataMap.putDouble(KEY_LATITUDE, location.latitude)
            putDataMapReq.dataMap.putDouble(KEY_LONGITUDE, location.longitude)
            putDataMapReq.dataMap.putDouble(KEY_ALTITUDE, location.altitude)
        } else {
            putDataMapReq.dataMap.putDouble(KEY_LATITUDE, 0.0)
            putDataMapReq.dataMap.putDouble(KEY_LONGITUDE, 0.0)
            putDataMapReq.dataMap.putDouble(KEY_ALTITUDE, 0.0)
        }

        val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
        dataClient.putDataItem(putDataReq).addOnSuccessListener {
            Log.d(TAG, "Data broadcasted successfully: isTracking=$isTracking, loc=$location")
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to broadcast data", e)
        }
    }
}
