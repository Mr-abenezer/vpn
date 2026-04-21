package com.bdnet.vpn.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.IBinder
import android.util.Log
import com.bdnet.vpn.tunnel.PsiphonLib
import com.bdnet.vpn.tunnel.PsiphonConfig
import com.bdnet.vpn.util.Constants

class PsiphonVpnService : VpnService() {

    private var isRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "PsiphonVpnService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_SERVICE_STOP -> {
                stopPsiphon()
            }
            else -> {
                startPsiphon(intent)
            }
        }
        return START_STICKY
    }

    private fun startPsiphon(intent: Intent?) {
        try {
            val host = intent?.getStringExtra("host") ?: return
            val port = intent?.getIntExtra("port", 443) ?: 443
            val password = intent?.getStringExtra("password") ?: ""

            val config = PsiphonConfig(
                serverHost = host,
                serverPort = port,
                authPassword = password
            )

            // Build VPN service
            val builder = Builder()
                .setSession("BD NET VPN - Psiphon")
                .addAddress(Constants.DEFAULT_VPN_IP, Constants.DEFAULT_VPN_SUBNET)
                .addRoute(Constants.VPN_ROUTE, Constants.VPN_ROUTE_PREFIX)
                .addDnsServer(Constants.DEFAULT_DNS_PRIMARY)
                .setMtu(1500)

            val tunFd = builder.establish()

            if (tunFd != null) {
                PsiphonLib.start(config, tunFd.fd)
                isRunning = true
                Log.i(TAG, "Psiphon tunnel started")
            } else {
                Log.e(TAG, "Failed to establish TUN interface")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Psiphon", e)
        }
    }

    private fun stopPsiphon() {
        PsiphonLib.stop()
        isRunning = false
        stopSelf()
        Log.i(TAG, "Psiphon tunnel stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (isRunning) {
            stopPsiphon()
        }
    }

    companion object {
        private const val TAG = "PsiphonVpnService"
    }
}
