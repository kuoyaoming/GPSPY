package app.gpstracker.android.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import app.gpstracker.android.data.database.SessionSummary
import app.gpstracker.android.ui.viewmodels.HistoryViewModel
import app.gpstracker.android.utils.GpxGenerator
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel(), modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sessions by viewModel.sessions.collectAsState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Recorded Sessions",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recorded sessions found.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sessions) { session ->
                    SessionItem(
                        session = session,
                        onExport = {
                            scope.launch {
                                val points = viewModel.getPointsForSession(session.sessionId)
                                if (points.isNotEmpty()) {
                                    val gpxString = GpxGenerator.generateGpx(points, "Session ${session.sessionId}")
                                    exportGpxFile(context, gpxString, "Session_${session.sessionId}.gpx")
                                } else {
                                    Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDelete = {
                            viewModel.deleteSession(session.sessionId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: SessionSummary, onExport: () -> Unit, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val startTimeStr = dateFormat.format(Date(session.startTime))
    val durationSec = session.durationMs / 1000
    val durationMin = durationSec / 60
    val durationRemSec = durationSec % 60
    val durationStr = String.format("%02d:%02d", durationMin, durationRemSec)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Session #${session.sessionId}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Start: $startTimeStr", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Duration: $durationStr", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onExport) {
                    Text("Export GPX")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            }
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
