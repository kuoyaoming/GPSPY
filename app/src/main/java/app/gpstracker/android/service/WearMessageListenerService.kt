package com.gpsspy.gpstracker.service

import android.content.Intent
import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearMessageListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearMessageListener"
        const val PATH_CONTROL_START = "/track_control/start"
        const val PATH_CONTROL_STOP = "/track_control/stop"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Received message: ${messageEvent.path}")

        try {
            when (messageEvent.path) {
                PATH_CONTROL_START -> {
                    val intent = Intent(this, LocationTrackingService::class.java).apply {
                        action = LocationTrackingService.ACTION_START
                    }
                    startForegroundService(intent)
                }
                PATH_CONTROL_STOP -> {
                    val intent = Intent(this, LocationTrackingService::class.java).apply {
                        action = LocationTrackingService.ACTION_STOP
                    }
                    startForegroundService(intent)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start service from background. Android 12+ limits.", e)
            // WearableListenerService often gets a temporary exemption, but in case it fails,
            // we catch it so the app doesn't crash.
        }
    }
}
