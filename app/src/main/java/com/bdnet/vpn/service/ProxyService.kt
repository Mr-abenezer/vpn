package com.bdnet.vpn.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.net.InetSocketAddress
import java.net.ServerSocket

class ProxyService : Service() {

    private var isRunning = false
    private var serverSocket: ServerSocket? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "ProxyService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "ACTION_START_PROXY" -> {
                val port = intent.getIntExtra("port", 8080)
                startProxy(port)
            }
            "ACTION_STOP_PROXY" -> {
                stopProxy()
            }
        }
        return START_STICKY
    }

    private fun startProxy(port: Int) {
        Thread {
            try {
                serverSocket = ServerSocket()
                serverSocket?.bind(InetSocketAddress(port))
                isRunning = true
                Log.i(TAG, "Proxy server started on port $port")

                while (isRunning) {
                    try {
                        val client = serverSocket?.accept()
                        // Handle client connection
                        client?.let { handleClient(it) }
                    } catch (e: Exception) {
                        if (isRunning) {
                            Log.e(TAG, "Error accepting client", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Proxy server error", e)
            }
        }.start()
    }

    private fun handleClient(client: java.net.Socket) {
        // Proxy traffic handling
        // This would route traffic through the VPN tunnel
    }

    private fun stopProxy() {
        isRunning = false
        serverSocket?.close()
        serverSocket = null
        stopSelf()
        Log.i(TAG, "Proxy server stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopProxy()
    }

    companion object {
        private const val TAG = "ProxyService"
    }
}
