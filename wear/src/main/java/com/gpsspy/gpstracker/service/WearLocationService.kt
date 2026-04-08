package com.gpsspy.gpstracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

class WearLocationService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START_STANDALONE"
        const val ACTION_STOP = "ACTION_STOP_STANDALONE"
        private const val CHANNEL_ID = "WearLocationServiceChannel"
        private const val NOTIFICATION_ID = 2
    }

    private lateinit var wakeLock: PowerManager.WakeLock
    private var isRecording = false

    override fun onCreate() {
        super.onCreate()
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GpsTracker::WearTrackingWakeLock")
        
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!isRecording) {
                    startForegroundService()
                    startStandaloneTracking()
                }
            }
            ACTION_STOP -> stopStandaloneTracking()
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPSPY Standalone")
            .setContentText("Recording location autonomously...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startStandaloneTracking() {
        isRecording = true
        if (!wakeLock.isHeld) wakeLock.acquire()
        
        // TODO: Implement FusedLocationProviderClient for standalone watch recording.
        // For Phase 3, we initialize the service. The actual location fetching would mimic the mobile app's LocationTrackingService.
    }

    private fun stopStandaloneTracking() {
        isRecording = false
        if (wakeLock.isHeld) wakeLock.release()
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Wear Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
