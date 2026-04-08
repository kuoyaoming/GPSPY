package com.gpsspy.gpstracker.service

import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchSyncService : WearableListenerService() {

    companion object {
        const val START_TRACKING_PATH = "/start-tracking"
        const val STOP_TRACKING_PATH = "/stop-tracking"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d("WatchSyncService", "Received message: ${messageEvent.path}")

        when (messageEvent.path) {
            START_TRACKING_PATH -> {
                val startIntent = Intent(this@WatchSyncService, LocationTrackingService::class.java)
                startIntent.setAction(LocationTrackingService.ACTION_START)
                ContextCompat.startForegroundService(this@WatchSyncService, startIntent)
            }
            STOP_TRACKING_PATH -> {
                val stopIntent = Intent(this@WatchSyncService, LocationTrackingService::class.java)
                stopIntent.setAction(LocationTrackingService.ACTION_STOP)
                startService(stopIntent)
            }
        }
    }
}
