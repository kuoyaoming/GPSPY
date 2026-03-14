package com.gpsspy.gpstracker.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PermissionScreen(
    onPermissionsGranted: () -> Unit
) {
    var showBackgroundDisclosure by remember { mutableStateOf(false) }
    var locationGranted by remember { mutableStateOf(false) }

    // Foreground Location Permission Launcher
    val foregroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            locationGranted = true
            showBackgroundDisclosure = true // Show prominent disclosure before requesting background
        }
    }

    // Background Location Permission Launcher
    val backgroundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionsGranted()
        }
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        // Proceeding anyway since it's just for the FGS notification, but ideally handle denial gracefully
        requestForegroundLocation(foregroundPermissionLauncher)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!showBackgroundDisclosure) {
            Text(
                text = "Welcome to GPS Tracker",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "To track your trajectory, we need location and notification permissions.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    requestForegroundLocation(foregroundPermissionLauncher)
                }
            }) {
                Text("Grant Permissions")
            }
        } else {
            // PROMINENT DISCLOSURE SCREEN (Crucial for Google Play Policy)
            Text(
                text = "Background Location Required",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "GPS Tracker collects location data to enable recording of your 3D movement trajectory even when the app is closed or not in use. This is necessary for long hikes or flights where you might turn off your screen to save battery.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    onPermissionsGranted()
                }
            }) {
                Text("I Understand")
            }
        }
    }
}

private fun requestForegroundLocation(launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>) {
    launcher.launch(
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
}

