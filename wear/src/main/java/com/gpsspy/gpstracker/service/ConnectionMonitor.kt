package com.gpsspy.gpstracker.service

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class ConnectionMonitor(private val context: Context) {

    private val capabilityClient = Wearable.getCapabilityClient(context)
    
    // We assume the mobile app advertises a capability like "gpsspy_mobile_app"
    // However, a simpler immediate check is node connected status.
    
    fun checkConnectionAndTriggerFallback(isTrackingCurrently: Boolean) {
        Wearable.getNodeClient(context).connectedNodes.addOnSuccessListener { nodes ->
            val isConnectedToPhone = nodes.any { it.isNearby }
            
            Log.d("ConnectionMonitor", "Connected to Phone: $isConnectedToPhone, IsTracking: $isTrackingCurrently")
            
            if (isTrackingCurrently && !isConnectedToPhone) {
                // Connection lost while tracking was active -> Start standalone fallback
                startFallbackService()
            } else if (isConnectedToPhone) {
                // Connected -> Stop fallback if it was running, merge data, and rely on phone
                stopFallbackService()
            }
        }.addOnFailureListener {
            Log.e("ConnectionMonitor", "Failed to retrieve connected nodes", it)
        }
    }

    private fun startFallbackService() {
        val intent = Intent(context, WearLocationService::class.java).apply {
            action = WearLocationService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    private fun stopFallbackService() {
        val intent = Intent(context, WearLocationService::class.java).apply {
            action = WearLocationService.ACTION_STOP
        }
        context.startService(intent)
    }
}
