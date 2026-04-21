package com.bdnet.vpn.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.bdnet.vpn.tunnel.HttpTunnelLib

class FWPuncherService : Service() {

    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "FWPuncherService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START" -> {
                val host = intent.getStringExtra("host")
                val port = intent.getIntExtra("port", 443)
                val payload = intent.getStringExtra("payload")
                startFirewallPuncher(host, port, payload)
            }
            "ACTION_STOP" -> {
                stopFirewallPuncher()
            }
        }
        return START_STICKY
    }

    private fun startFirewallPuncher(host: String?, port: Int, payload: String?) {
        if (host != null) {
            HttpTunnelLib.start(host, port, payload, null)
            isRunning = true
            Log.i(TAG, "Firewall puncher started for $host:$port")
        }
    }

    private fun stopFirewallPuncher() {
        HttpTunnelLib.stop()
        isRunning = false
        stopSelf()
        Log.i(TAG, "Firewall puncher stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) {
            stopFirewallPuncher()
        }
    }

    companion object {
        private const val TAG = "FWPuncherService"
    }
}
