package app.gpstracker.android.ui.screens

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gpstracker.android.service.LocationTrackingService
import app.gpstracker.android.ui.viewmodels.TrackingViewModel
import app.gpstracker.android.utils.GpxGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun TrackingScreen(viewModel: TrackingViewModel = viewModel(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isTracking by viewModel.isTracking.collectAsState()
    val frequencyMs by viewModel.trackingFrequencyMs.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val satellites by viewModel.satellites.collectAsState()
    val scope = rememberCoroutineScope()

    var showGpsDisabledDialog by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val checkGps = {
            showGpsDisabledDialog = !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }

        checkGps()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    checkGps()
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    if (showGpsDisabledDialog) {
        AlertDialog(
            onDismissRequest = { /* Force user to enable or dismiss, maybe just dismissible */ },
            title = { Text("GPS is Disabled") },
            text = { Text("Your GPS seems to be disabled. Please enable it in the system settings to allow tracking.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    context.startActivity(intent)
                }) {
                    Text("Enable GPS")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGpsDisabledDialog = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    Column(
        modifier = modifier
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
                },
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                Text(text = if (isTracking) "Stop Tracking" else "Start Tracking")
            }
        }

        if (isTracking && currentLocation != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Current GPS Data:", style = MaterialTheme.typography.titleMedium)
                Text(text = "Lat: ${currentLocation?.latitude}, Lon: ${currentLocation?.longitude}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Alt: ${currentLocation?.altitude}m, Speed: ${currentLocation?.speed}m/s", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "GNSS Info:", style = MaterialTheme.typography.titleMedium)
                val constellationMap = mapOf(
                    1 to "GPS", 2 to "SBAS", 3 to "GLONASS", 4 to "QZSS", 5 to "BEIDOU", 6 to "GALILEO", 7 to "IRNSS"
                )
                val usedConstellations = satellites.filter { it.usedInFix }.map { it.constellationType }.toSet()
                val availableConstellations = satellites.map { it.constellationType }.toSet()

                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    availableConstellations.forEach { type ->
                        val name = constellationMap[type] ?: "Unknown ($type)"
                        val color = if (usedConstellations.contains(type)) Color.Green else Color.Red
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
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
