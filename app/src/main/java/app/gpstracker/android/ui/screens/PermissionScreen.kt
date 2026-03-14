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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gpsspy.gpstracker.R

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
                text = stringResource(R.string.perm_welcome),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.perm_description),
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
                Text(stringResource(R.string.perm_grant_button))
            }
        } else {
            // PROMINENT DISCLOSURE SCREEN (Crucial for Google Play Policy)
            Text(
                text = stringResource(R.string.perm_bg_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.perm_bg_description),
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
                Text(stringResource(R.string.perm_understand_button))
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

