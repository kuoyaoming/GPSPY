package com.example.gpstracker.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gpstracker.service.LocationTrackingService
import com.example.gpstracker.ui.viewmodels.TrackingViewModel
import com.example.gpstracker.utils.GpxGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun TrackingScreen(viewModel: TrackingViewModel = viewModel()) {
    val context = LocalContext.current
    val isTracking = viewModel.isTracking
    val frequencyMs by viewModel.trackingFrequencyMs.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Title
        Text(
            text = "GPS Tracking Session",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        // Main Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isTracking) "Status: RECORDING..." else "Status: IDLE",
                style = MaterialTheme.typography.titleMedium,
                color = if (isTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(context, LocationTrackingService::class.java).apply {
                        action = if (isTracking) LocationTrackingService.ACTION_STOP else LocationTrackingService.ACTION_START
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                    viewModel.setTrackingState(!isTracking)
                },
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Text(text = if (isTracking) "Stop Tracking" else "Start Tracking")
            }
        }

        // Frequency Control
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Recording Frequency: ${frequencyMs / 1000} seconds")
            Slider(
                value = frequencyMs.toFloat(),
                onValueChange = { viewModel.updateFrequency(it.toLong()) },
                valueRange = 1000f..60000f, // 1 sec to 60 sec
                steps = 59,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                text = "Dynamic updates - no need to stop tracking",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Export Control
        Button(
            onClick = {
                scope.launch {
                    val sessionId = viewModel.getLatestSessionId()
                    if (sessionId != null) {
                        val points = viewModel.getPointsForSession(sessionId)
                        if (points.isNotEmpty()) {
                            val gpxString = GpxGenerator.generateGpx(points, "Session $sessionId")
                            exportGpxFile(context, gpxString, "Session_$sessionId.gpx")
                        } else {
                            Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "No tracking sessions found", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.padding(bottom = 32.dp),
            enabled = !isTracking // Prevent export while active to ensure data consistency
        ) {
            Text("Export Latest Session as GPX")
        }
    }
}

private fun exportGpxFile(context: Context, gpxContent: String, fileName: String) {
    try {
        val path = File(context.cacheDir, "gpx_exports")
        if (!path.exists()) path.mkdirs()

        val file = File(path, fileName)
        FileOutputStream(file).use {
            it.write(gpxContent.toByteArray())
        }

        // Use FileProvider to share the file securely
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/gpx+xml"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Export GPX"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
