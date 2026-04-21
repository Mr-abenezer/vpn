package com.bdnet.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.bdnet.vpn.dataStore
import com.bdnet.vpn.service.TunnelVpnService
import com.bdnet.vpn.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                checkAutoConnect(context)
            }
        }
    }

    private fun checkAutoConnect(context: Context) {
        scope.launch {
            try {
                val prefs = context.dataStore.data.first()
                val autoConnect = prefs[booleanPreferencesKey(Constants.PREF_AUTO_CONNECT)] ?: false

                if (autoConnect) {
                    Log.i(TAG, "Auto-connect enabled, starting VPN")
                    val serviceIntent = Intent(context, TunnelVpnService::class.java)
                    context.startForegroundService(serviceIntent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking auto-connect", e)
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
