package com.gpsspy.gpstracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ActivityUpdateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var routineAnalysisManager: RoutineAnalysisManager

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ActivityUpdateReceiver", "Received Broadcast!")
        if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            if (result != null) {
                val mostProbableActivity = result.mostProbableActivity
                Log.d("ActivityUpdateReceiver", "Most probable activity: ${mostProbableActivity.type} with confidence ${mostProbableActivity.confidence}")
                routineAnalysisManager.updateDetectedActivity(mostProbableActivity.type)
            }
        }
    }
}
