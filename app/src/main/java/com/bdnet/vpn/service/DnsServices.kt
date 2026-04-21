package com.bdnet.vpn.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.bdnet.vpn.tunnel.DnsTunnelLib

abstract class BaseDnsService : Service() {

    protected var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopDns()
    }

    protected abstract fun stopDns()
}

class SocksDNSService : BaseDnsService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        Log.i(TAG, "SocksDNSService started")
        return START_STICKY
    }

    override fun stopDns() {
        isRunning = false
        stopSelf()
    }

    companion object {
        private const val TAG = "SocksDNSService"
    }
}

class PsiphonDNSService : BaseDnsService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        Log.i(TAG, "PsiphonDNSService started")
        return START_STICKY
    }

    override fun stopDns() {
        isRunning = false
        stopSelf()
    }

    companion object {
        private const val TAG = "PsiphonDNSService"
    }
}

class XRayDNSService : BaseDnsService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        Log.i(TAG, "XRayDNSService started")
        return START_STICKY
    }

    override fun stopDns() {
        isRunning = false
        stopSelf()
    }

    companion object {
        private const val TAG = "XRayDNSService"
    }
}
