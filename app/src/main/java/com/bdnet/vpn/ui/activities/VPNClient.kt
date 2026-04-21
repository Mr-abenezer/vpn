package com.bdnet.vpn.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bdnet.vpn.R
import com.bdnet.vpn.data.local.AppDatabase
import com.bdnet.vpn.data.model.Server
import com.bdnet.vpn.service.TunnelVpnService
import com.bdnet.vpn.tunnel.ConnectionState
import com.bdnet.vpn.tunnel.SpeedStats
import com.bdnet.vpn.tunnel.TunnelConfig
import com.bdnet.vpn.tunnel.TunnelManager
import com.bdnet.vpn.ui.adapters.ServerAdapter
import com.bdnet.vpn.util.Constants
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VPNClient : AppCompatActivity(), ServerAdapter.OnServerClickListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var bottomNavigation: BottomNavigationView

    private lateinit var connectButton: Button
    private lateinit var connectionStatus: TextView
    private lateinit var connectionTimer: TextView
    private lateinit var ipAddress: TextView
    private lateinit var downloadSpeed: TextView
    private lateinit var uploadSpeed: TextView
    private lateinit var serverName: TextView
    private lateinit var serverCarrier: TextView
    private lateinit var serverFlag: ImageView
    private lateinit var serverCard: View
    private lateinit var adContainer: FrameLayout

    private val tunnelManager = TunnelManager.getInstance()
    private val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    private val handler = Handler(Looper.getMainLooper())

    private var isConnecting = false
    private var isConnected = false
    private var connectionStartTime = 0L
    private var selectedServer: Server? = null

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isConnected) {
                updateTimer()
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val connectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status = intent.getIntExtra(Constants.EXTRA_STATUS, Constants.STATUS_DISCONNECTED)
            when (status) {
                Constants.STATUS_CONNECTED -> onConnectionStateChanged(ConnectionState.Connected)
                Constants.STATUS_DISCONNECTED -> onConnectionStateChanged(ConnectionState.Disconnected)
                Constants.STATUS_ERROR -> {
                    val message = intent.getStringExtra(Constants.EXTRA_STATUS)
                    onConnectionStateChanged(ConnectionState.Error(message ?: "Unknown error"))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpn_client)

        initViews()
        setupNavigation()
        setupClickListeners()
        observeConnectionState()
        observeSpeedStats()
        loadLastServer()
        setupAdMob()
        registerReceiver()

        // Check VPN permission
        checkVpnPermission()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        connectButton = findViewById(R.id.connect_button)
        connectionStatus = findViewById(R.id.connection_status)
        connectionTimer = findViewById(R.id.connection_timer)
        ipAddress = findViewById(R.id.ip_address)
        downloadSpeed = findViewById(R.id.download_speed)
        uploadSpeed = findViewById(R.id.upload_speed)
        serverName = findViewById(R.id.server_name)
        serverCarrier = findViewById(R.id.server_carrier)
        serverFlag = findViewById(R.id.server_flag)
        serverCard = findViewById(R.id.server_card)
        adContainer = findViewById(R.id.ad_container)
    }

    private fun setupNavigation() {
        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home
                    true
                }
                R.id.nav_servers -> {
                    openServerList()
                    true
                }
                R.id.nav_settings -> {
                    openSettings()
                    true
                }
                else -> false
            }
        }

        // Drawer Navigation
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_networks -> {
                    openServerList()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_split_tunnel -> {
                    openSplitTunnel()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_vpn_share -> {
                    openVpnShare()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_ip_hunter -> {
                    openIpHunter()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_import_tweak -> {
                    openImportTweak()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_dns_settings -> {
                    openDnsSettings()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_logs -> {
                    openLogs()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_stats -> {
                    openStats()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_about -> {
                    openAbout()
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_exit -> {
                    exitApp()
                    drawerLayout.closeDrawers()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        // Menu button opens drawer
        findViewById<ImageView>(R.id.menu_button).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Notification button
        findViewById<ImageView>(R.id.notification_button).setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
        }

        // Connect button
        connectButton.setOnClickListener {
            if (isConnected) {
                disconnectVpn()
            } else if (!isConnecting) {
                connectVpn()
            }
        }

        // Server card opens server list
        serverCard.setOnClickListener {
            openServerList()
        }
    }

    private fun observeConnectionState() {
        lifecycleScope.launch {
            tunnelManager.connectionState.collectLatest { state ->
                onConnectionStateChanged(state)
            }
        }
    }

    private fun observeSpeedStats() {
        lifecycleScope.launch {
            tunnelManager.speedStats.collectLatest { stats ->
                updateSpeedDisplay(stats)
            }
        }
    }

    private fun onConnectionStateChanged(state: ConnectionState) {
        when (state) {
            is ConnectionState.Connecting -> {
                isConnecting = true
                isConnected = false
                connectButton.text = getString(R.string.connecting)
                connectButton.isEnabled = false
                connectionStatus.text = getString(R.string.connecting)
                connectionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                connectionTimer.visibility = View.GONE
            }
            is ConnectionState.Connected -> {
                isConnecting = false
                isConnected = true
                connectionStartTime = System.currentTimeMillis()
                connectButton.text = getString(R.string.disconnect)
                connectButton.setBackgroundResource(R.drawable.connect_button_connected)
                connectButton.isEnabled = true
                connectionStatus.text = getString(R.string.connected)
                connectionStatus.setTextColor(ContextCompat.getColor(this, R.color.connected))
                connectionTimer.visibility = View.VISIBLE
                handler.post(timerRunnable)
                fetchIpAddress()
            }
            is ConnectionState.Disconnected -> {
                isConnecting = false
                isConnected = false
                connectButton.text = getString(R.string.connect)
                connectButton.setBackgroundResource(R.drawable.connect_button_bg)
                connectButton.isEnabled = true
                connectionStatus.text = getString(R.string.disconnected)
                connectionStatus.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                connectionTimer.visibility = View.GONE
                connectionTimer.text = "00:00:00"
                ipAddress.text = "--.--.--.--"
                downloadSpeed.text = "0 KB/s"
                uploadSpeed.text = "0 KB/s"
                handler.removeCallbacks(timerRunnable)
            }
            is ConnectionState.Error -> {
                isConnecting = false
                isConnected = false
                connectButton.text = getString(R.string.connect)
                connectButton.setBackgroundResource(R.drawable.connect_button_bg)
                connectButton.isEnabled = true
                connectionStatus.text = getString(R.string.connection_failed)
                connectionStatus.setTextColor(ContextCompat.getColor(this, R.color.error))
                connectionTimer.visibility = View.GONE
                Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                handler.removeCallbacks(timerRunnable)
            }
        }
    }

    private fun updateSpeedDisplay(stats: SpeedStats) {
        downloadSpeed.text = formatSpeed(stats.speedIn)
        uploadSpeed.text = formatSpeed(stats.speedOut)
    }

    private fun formatSpeed(speed: Long): String {
        return when {
            speed >= 1_000_000 -> String.format("%.1f MB/s", speed / 1_000_000.0)
            speed >= 1_000 -> String.format("%.1f KB/s", speed / 1_000.0)
            else -> "$speed B/s"
        }
    }

    private fun updateTimer() {
        val elapsed = System.currentTimeMillis() - connectionStartTime
        val seconds = (elapsed / 1000) % 60
        val minutes = (elapsed / (1000 * 60)) % 60
        val hours = elapsed / (1000 * 60 * 60)

        connectionTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun fetchIpAddress() {
        lifecycleScope.launch {
            // Fetch IP from API
            try {
                // Simulated - implement actual API call
                ipAddress.text = "Loading..."
            } catch (e: Exception) {
                ipAddress.text = "Unknown"
            }
        }
    }

    private fun connectVpn() {
        val intent = Intent(this, VpnService::class.java)
        val prepareResult = VpnService.prepare(this)
        if (prepareResult != null) {
            startActivityForResult(prepareResult, VPN_PERMISSION_REQUEST)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val server = selectedServer ?: run {
            // Get last used or auto server
            lifecycleScope.launch {
                val lastServer = database.serverDao().getLastUsedServer()
                if (lastServer != null) {
                    selectedServer = Server.fromEntity(lastServer)
                    startVpnWithServer(selectedServer!!)
                } else {
                    Toast.makeText(this@VPNClient, "No server selected", Toast.LENGTH_SHORT).show()
                }
            }
            return
        }
        startVpnWithServer(server)
    }

    private fun startVpnWithServer(server: Server) {
        val intent = Intent(this, TunnelVpnService::class.java)
        intent.putExtra("server_id", server.id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun disconnectVpn() {
        val intent = Intent(this, TunnelVpnService::class.java)
        intent.action = Constants.ACTION_SERVICE_STOP
        startService(intent)
    }

    private fun loadLastServer() {
        lifecycleScope.launch {
            val lastServer = database.serverDao().getLastUsedServer()
            if (lastServer != null) {
                selectedServer = Server.fromEntity(lastServer)
                updateServerDisplay(selectedServer!!)
            } else {
                // Default to auto select
                serverName.text = getString(R.string.auto_select)
                serverCarrier.text = "Best available server"
            }
        }
    }

    private fun updateServerDisplay(server: Server) {
        serverName.text = server.name
        serverCarrier.text = server.carrier ?: server.countryCode
        // Load flag based on country code
        val flagResId = resources.getIdentifier(
            "flag_${server.countryCode.lowercase()}",
            "drawable",
            packageName
        )
        if (flagResId != 0) {
            serverFlag.setImageResource(flagResId)
        } else {
            serverFlag.setImageResource(R.drawable.ic_flag_default)
        }
    }

    private fun checkVpnPermission() {
        val intent = Intent(this, VpnService::class.java)
        val prepareResult = VpnService.prepare(this)
        if (prepareResult == null) {
            // Already have permission
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            startVpnService()
        }
    }

    private fun setupAdMob() {
        if (Constants.ADMOB_BANNER_UNIT_ID.contains("X")) {
            return
        }
        val adView = AdView(this)
        adView.adUnitId = Constants.ADMOB_BANNER_UNIT_ID
        adView.setAdSize(AdSize.BANNER)
        adView.loadAd(AdRequest.Builder().build())
        adContainer.addView(adView)
    }

    private fun registerReceiver() {
        val filter = IntentFilter(Constants.ACTION_CONNECTION_STATUS)
        ContextCompat.registerReceiver(
            this,
            connectionReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        try {
            unregisterReceiver(connectionReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }

    // Navigation methods
    private fun openServerList() {
        startActivity(Intent(this, ServerListActivity::class.java))
    }

    private fun openSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openSplitTunnel() {
        startActivity(Intent(this, AppListActivity::class.java))
    }

    private fun openVpnShare() {
        startActivity(Intent(this, VPNShareActivity::class.java))
    }

    private fun openIpHunter() {
        startActivity(Intent(this, IPHunterActivity::class.java))
    }

    private fun openImportTweak() {
        startActivity(Intent(this, ImportTweakActivity::class.java))
    }

    private fun openDnsSettings() {
        // Open settings with DNS section
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun openLogs() {
        startActivity(Intent(this, LogsActivity::class.java))
    }

    private fun openStats() {
        startActivity(Intent(this, StatsActivity::class.java))
    }

    private fun openAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private fun exitApp() {
        finishAffinity()
    }

    override fun onServerClick(server: Server) {
        selectedServer = server
        updateServerDisplay(server)
        lifecycleScope.launch {
            database.serverDao().updateLastUsed(server.id)
        }
    }

    companion object {
        private const val VPN_PERMISSION_REQUEST = 1001
    }
}
