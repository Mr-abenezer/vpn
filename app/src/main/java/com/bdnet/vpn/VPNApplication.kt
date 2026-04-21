package com.bdnet.vpn

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.bdnet.vpn.data.local.AppDatabase
import com.bdnet.vpn.util.Constants
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class VPNApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        try {
            FirebaseApp.initializeApp(this)
        } catch (_: Exception) {
        }

        try {
            MobileAds.initialize(this)
        } catch (_: Exception) {
        }

        // Create notification channels
        createNotificationChannels()

        setupCrashReporting()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // VPN Status Channel (High Importance - Persistent)
        val statusChannel = NotificationChannel(
            Constants.CHANNEL_VPN_STATUS,
            getString(R.string.channel_vpn_status),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "VPN connection status notifications"
            setShowBadge(false)
            enableVibration(false)
            enableLights(false)
        }

        // VPN Updates Channel (Default Importance)
        val updatesChannel = NotificationChannel(
            Constants.CHANNEL_VPN_UPDATES,
            getString(R.string.channel_vpn_updates),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Server updates and announcements"
            setShowBadge(true)
        }

        // VPN Alerts Channel (High Importance)
        val alertsChannel = NotificationChannel(
            Constants.CHANNEL_VPN_ALERTS,
            getString(R.string.channel_vpn_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Important alerts and warnings"
            setShowBadge(true)
            enableVibration(true)
            enableLights(true)
        }

        notificationManager.createNotificationChannels(
            listOf(statusChannel, updatesChannel, alertsChannel)
        )
    }

    private fun setupCrashReporting() {
        try {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (_: Exception) {
        }
    }

    companion object {
        lateinit var instance: VPNApplication
            private set

        fun getAppContext(): Context = instance.applicationContext
    }
}
