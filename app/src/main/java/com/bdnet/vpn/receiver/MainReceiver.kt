package com.bdnet.vpn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bdnet.vpn.service.TunnelVpnService
import com.bdnet.vpn.util.Constants

class MainReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.i(TAG, "Received action: $action")

        when (action) {
            Constants.ACTION_SERVICE_STOP -> {
                stopVpnService(context)
            }
            Constants.ACTION_SERVICE_RESTART -> {
                restartVpnService(context)
            }
        }
    }

    private fun stopVpnService(context: Context) {
        val serviceIntent = Intent(context, TunnelVpnService::class.java)
        serviceIntent.action = Constants.ACTION_SERVICE_STOP
        context.startService(serviceIntent)
    }

    private fun restartVpnService(context: Context) {
        stopVpnService(context)

        // Wait a moment then restart
        Thread {
            Thread.sleep(1000)
            val serviceIntent = Intent(context, TunnelVpnService::class.java)
            context.startService(serviceIntent)
        }.start()
    }

    companion object {
        private const val TAG = "MainReceiver"
    }
}
