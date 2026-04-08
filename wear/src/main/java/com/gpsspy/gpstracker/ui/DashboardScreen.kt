package com.gpsspy.gpstracker.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.*
import com.google.android.gms.wearable.*

@Composable
fun DashboardScreen(context: Context) {
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var altitude by remember { mutableDoubleStateOf(0.0) }
    var isTracking by remember { mutableStateOf(false) }

    val listState = rememberScalingLazyListState()

    DisposableEffect(context) {
        val dataClient = Wearable.getDataClient(context)

        val listener = DataClient.OnDataChangedListener { dataEvents ->
            for (event in dataEvents) {
                if (event.type == DataEvent.TYPE_CHANGED) {
                    val path = event.dataItem.uri.path
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                    if (path == "/location-update") {
                        latitude = dataMap.getDouble("latitude")
                        longitude = dataMap.getDouble("longitude")
                        altitude = dataMap.getDouble("altitude")
                    } else if (path == "/tracking-status") {
                        isTracking = dataMap.getBoolean("isTracking")
                    }
                }
            }
        }

        dataClient.addListener(listener)
        onDispose {
            dataClient.removeListener(listener)
        }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "GPSPY",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                item {
                    Text(
                        text = String.format("Alt: %.1fm", altitude),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                item {
                    Text(
                        text = String.format("Lat: %.4f\nLon: %.4f", latitude, longitude),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    Button(
                        onClick = {
                            val messageClient = Wearable.getMessageClient(context)
                            val path = if (isTracking) "/stop-tracking" else "/start-tracking"

                            // Send to all connected nodes
                            val nodeClient = Wearable.getNodeClient(context)
                            nodeClient.connectedNodes.addOnSuccessListener { nodes ->
                                for (node in nodes) {
                                    messageClient.sendMessage(node.id, path, ByteArray(0))
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = if (isTracking) "STOP" else "START",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item {
                    Text(
                        text = if (isTracking) "Recording..." else "Ready",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isTracking) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
