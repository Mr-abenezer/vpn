package com.bdnet.vpn.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bdnet.vpn.VPNApplication
import com.bdnet.vpn.R
import com.bdnet.vpn.data.local.entity.ServerEntity
import com.bdnet.vpn.data.repository.ConnectionHistory
import com.bdnet.vpn.data.repository.ConnectionRepository
import com.bdnet.vpn.tunnel.ConnectionState
import com.bdnet.vpn.tunnel.SpeedStats
import com.bdnet.vpn.tunnel.TunnelConfig
import com.bdnet.vpn.tunnel.TunnelManager
import com.bdnet.vpn.ui.activities.VPNClient
import com.bdnet.vpn.util.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class TunnelVpnService : VpnService(), CoroutineScope {

    private val job = SupervisorJob()
    override val coroutineContext = Dispatchers.IO + job

    private val binder = LocalBinder()
    private val tunnelManager = TunnelManager.getInstance()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var tunFd: ParcelFileDescriptor? = null
    private var connectionStartTime = 0L
    private var currentConfig: TunnelConfig? = null

    private var connectionHistoryId: Long = 0
    private var bytesInTotal = 0L
    private var bytesOutTotal = 0L

    inner class LocalBinder : Binder() {
        fun getService(): TunnelVpnService = this@TunnelVpnService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_SERVICE_STOP -> {
                stopVPN()
            }
            else -> {
                val serverId = intent?.getLongExtra("server_id", -1L) ?: -1L
                if (serverId > 0) {
                    launch {
                        val server = VPNApplication.instance.database.serverDao().getServerById(serverId)
                        server?.let { startVPN(buildTunnelConfig(it)) }
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun setupNotificationChannel() = Unit

    private fun buildTunnelConfig(server: ServerEntity): TunnelConfig {
        val prefs = getSharedPreferences("vpn_prefs", MODE_PRIVATE)
        val dnsPrimary = prefs.getString(Constants.PREF_DNS_PRIMARY, Constants.DEFAULT_DNS_PRIMARY) ?: Constants.DEFAULT_DNS_PRIMARY
        val dnsSecondary = prefs.getString(Constants.PREF_DNS_SECONDARY, Constants.DEFAULT_DNS_SECONDARY) ?: Constants.DEFAULT_DNS_SECONDARY

        return TunnelConfig(
            server = com.bdnet.vpn.data.model.Server.fromEntity(server),
            dnsServers = listOf(dnsPrimary, dnsSecondary),
            sessionName = getString(R.string.app_name)
        )
    }

    private fun startVPN(config: TunnelConfig) {
        launch {
            try {
                currentConfig = config

                // Build VPN service
                val builder = Builder()
                builder.setSession(config.sessionName)
                    .addAddress(Constants.DEFAULT_VPN_IP, Constants.DEFAULT_VPN_SUBNET)
                    .addRoute(Constants.VPN_ROUTE, Constants.VPN_ROUTE_PREFIX)
                    .setMtu(config.mtu)
                    .setBlocking(false)

                config.dnsServers.forEach { dnsServer ->
                    builder.addDnsServer(dnsServer)
                }

                // Apply split tunnel if configured
                if (config.bypassApps.isNotEmpty()) {
                    config.bypassApps.forEach { packageName ->
                        try {
                            builder.addDisallowedApplication(packageName)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add disallowed app: $packageName", e)
                        }
                    }
                }

                if (config.includeApps.isNotEmpty()) {
                    config.includeApps.forEach { packageName ->
                        try {
                            builder.addAllowedApplication(packageName)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to add allowed app: $packageName", e)
                        }
                    }
                }

                // Establish TUN interface
                tunFd = builder.establish()
                    ?: throw IllegalStateException("Failed to establish VPN tunnel")

                tunnelManager.setTunFd(tunFd!!)

                // Start foreground notification
                startForeground(Constants.NOTIFICATION_VPN_ACTIVE, createNotification())

                // Start tunnel
                tunnelManager.start(config)

                // Record connection start
                connectionStartTime = System.currentTimeMillis()
                recordConnectionStart(config)

                // Observe connection state
                observeConnectionState()

                // Observe speed stats
                observeSpeedStats()

                // Setup network callback for connectivity changes
                setupNetworkCallback()

                Log.i(TAG, "VPN started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start VPN", e)
                stopVPN()
            }
        }
    }

    private fun observeConnectionState() {
        launch {
            tunnelManager.connectionState.collectLatest { state ->
                when (state) {
                    is ConnectionState.Connected -> {
                        updateNotification("Connected", null)
                    }
                    is ConnectionState.Connecting -> {
                        updateNotification("Connecting...", null)
                    }
                    is ConnectionState.Disconnected -> {
                        stopVPN()
                    }
                    is ConnectionState.Error -> {
                        updateNotification("Error: ${state.message}", null)
                        stopVPN()
                    }
                }
            }
        }
    }

    private fun observeSpeedStats() {
        launch {
            tunnelManager.speedStats.collectLatest { stats ->
                updateNotification(null, stats)
                bytesInTotal = stats.bytesIn
                bytesOutTotal = stats.bytesOut
            }
        }
    }

    private fun recordConnectionStart(config: TunnelConfig) {
        launch {
            val history = ConnectionHistory(
                serverId = config.server.id,
                serverName = config.server.name,
                protocol = config.server.protocol
            )
            connectionHistoryId = ConnectionRepository(
                VPNApplication.instance.database.connectionHistoryDao()
            ).addHistory(history)
        }
    }

    private fun recordConnectionEnd() {
        launch {
            val duration = System.currentTimeMillis() - connectionStartTime
            ConnectionRepository(
                VPNApplication.instance.database.connectionHistoryDao()
            ).endConnection(connectionHistoryId, bytesInTotal, bytesOutTotal, duration)
        }
    }

    private fun setupNetworkCallback() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // Network available - VPN should route through this
            }

            override fun onLost(network: Network) {
                // Network lost - may need to reconnect
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    private fun createNotification(): Notification {
        val disconnectIntent = Intent(this, TunnelVpnService::class.java).apply {
            action = Constants.ACTION_SERVICE_STOP
        }

        val disconnectPendingIntent = PendingIntent.getService(
            this,
            0,
            disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, VPNClient::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.CHANNEL_VPN_STATUS)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.connected))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_disconnect, getString(R.string.disconnect), disconnectPendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(statusText: String?, stats: SpeedStats?) {
        val notification = createNotification().let {
            val builder = NotificationCompat.Builder(this, Constants.CHANNEL_VPN_STATUS)
            builder.setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)

            if (statusText != null) {
                builder.setContentText(statusText)
            }

            if (stats != null) {
                val speedText = formatSpeed(stats.speedIn) + " ↓  " + formatSpeed(stats.speedOut) + " ↑"
                builder.setSubText(speedText)
            }

            builder.build()
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(Constants.NOTIFICATION_VPN_ACTIVE, notification)
    }

    private fun formatSpeed(speed: Long): String {
        return when {
            speed >= 1_000_000 -> String.format("%.1f MB/s", speed / 1_000_000.0)
            speed >= 1_000 -> String.format("%.1f KB/s", speed / 1_000.0)
            else -> "$speed B/s"
        }
    }

    fun stopVPN() {
        launch {
            tunnelManager.stop()
            tunFd?.close()
            tunFd = null

            networkCallback?.let {
                try {
                    val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                    connectivityManager.unregisterNetworkCallback(it)
                } catch (e: Exception) {
                    // Ignore
                }
            }
            networkCallback = null

            recordConnectionEnd()

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

            // Broadcast status
            sendBroadcast(Intent(Constants.ACTION_CONNECTION_STATUS).apply {
                putExtra(Constants.EXTRA_STATUS, Constants.STATUS_DISCONNECTED)
            })

            Log.i(TAG, "VPN stopped")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Handle memory pressure
    }

    companion object {
        private const val TAG = "TunnelVpnService"
    }
}
