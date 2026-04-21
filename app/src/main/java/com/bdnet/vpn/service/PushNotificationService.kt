package com.bdnet.vpn.service

import android.util.Log
import com.bdnet.vpn.data.local.AppDatabase
import com.bdnet.vpn.data.model.Server
import com.bdnet.vpn.util.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PushNotificationService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM token: ${token.take(8)}...")
        // Send token to server if needed
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.i(TAG, "FCM message received from: ${message.from}")

        message.data.isNotEmpty().let {
            when {
                message.data.containsKey("action") -> {
                    handleAction(message.data["action"]!!, message.data)
                }
                message.data.containsKey("servers") -> {
                    // Server list update
                    handleServerUpdate(message.data)
                }
                else -> {
                    // Show notification
                    showNotification(
                        message.notification?.title ?: "BD NET VPN",
                        message.notification?.body ?: ""
                    )
                }
            }
        }
    }

    private fun handleAction(action: String, data: Map<String, String>) {
        when (action) {
            "update_servers" -> handleServerUpdate(data)
            "announce" -> showNotification(
                data["title"] ?: "Announcement",
                data["message"] ?: ""
            )
            "force_update" -> {
                // Show force update dialog
            }
        }
    }

    private fun handleServerUpdate(data: Map<String, String>) {
        scope.launch {
            try {
                // Parse server data and update database
                val serversJson = data["servers"] ?: return@launch
                // Parse and insert servers
                Log.i(TAG, "Server list updated")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update servers", e)
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        // Show notification using NotificationCompat
    }

    companion object {
        private const val TAG = "PushNotificationService"
    }
}
