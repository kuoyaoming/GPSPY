package com.gpsspy.gpstracker

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.wear.ambient.AmbientModeSupport
import com.gpsspy.gpstracker.service.ConnectionMonitor
import com.gpsspy.gpstracker.ui.DashboardScreen

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {

    private lateinit var ambientController: AmbientModeSupport.AmbientController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ambientController = AmbientModeSupport.attach(this)
        
        setContent {
            DashboardScreen(this)
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback {
        return object : AmbientModeSupport.AmbientCallback() {
            override fun onEnterAmbient(ambientDetails: Bundle?) {
                super.onEnterAmbient(ambientDetails)
                // Handle entering ambient mode (e.g., lower refresh rate)
            }

            override fun onExitAmbient() {
                super.onExitAmbient()
                // Handle exiting ambient mode
            }

            override fun onUpdateAmbient() {
                super.onUpdateAmbient()
                // Update screen in ambient mode (called approx once a minute)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // We assume tracking is active here as a baseline for the example, 
        // in a complete flow we would read the current shared state.
        val connectionMonitor = ConnectionMonitor(this)
        connectionMonitor.checkConnectionAndTriggerFallback(isTrackingCurrently = true)
    }
}
