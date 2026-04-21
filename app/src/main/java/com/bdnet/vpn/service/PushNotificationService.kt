package com.bdnet.vpn.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class PushNotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
