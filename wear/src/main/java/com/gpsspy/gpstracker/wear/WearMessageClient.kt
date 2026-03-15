package com.gpsspy.gpstracker.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearMessageClient(private val context: Context) {

    companion object {
        private const val TAG = "WearMessageClient"
        const val PATH_CONTROL_START = "/track_control/start"
        const val PATH_CONTROL_STOP = "/track_control/stop"
    }

    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    suspend fun sendStartMessage() {
        sendMessage(PATH_CONTROL_START)
    }

    suspend fun sendStopMessage() {
        sendMessage(PATH_CONTROL_STOP)
    }

    private suspend fun sendMessage(path: String) {
        try {
            val nodes = nodeClient.connectedNodes.await()
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found to send message: $path")
                return
            }

            for (node in nodes) {
                messageClient.sendMessage(node.id, path, ByteArray(0)).await()
                Log.d(TAG, "Message sent to node ${node.id}: $path")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message: $path", e)
        }
    }
}
