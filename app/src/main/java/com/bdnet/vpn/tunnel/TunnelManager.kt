package com.bdnet.vpn.tunnel

import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TunnelManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _speedStats = MutableStateFlow(SpeedStats(0, 0, 0, 0))
    val speedStats: StateFlow<SpeedStats> = _speedStats.asStateFlow()

    private var tunFd: ParcelFileDescriptor? = null
    private var currentConfig: TunnelConfig? = null

    fun start(config: TunnelConfig) {
        currentConfig = config
        scope.launch {
            try {
                _connectionState.emit(ConnectionState.Connecting)

                when (config.server.protocol) {
                    "v2ray", "vmess", "vless", "xtls", "trojan", "shadowsocks" -> {
                        startXrayTunnel(config)
                    }
                    "psiphon" -> {
                        startPsiphonTunnel(config)
                    }
                    "udp", "udp_tunnel" -> {
                        startUdpTunnel(config)
                    }
                    "dns_tunnel" -> {
                        startDnsTunnel(config)
                    }
                    "ssh", "ssl", "http" -> {
                        startHttpSshTunnel(config)
                    }
                    else -> {
                        throw IllegalStateException("Unknown protocol: ${config.server.protocol}")
                    }
                }

                _connectionState.emit(ConnectionState.Connected)
                startSpeedMonitor()
            } catch (e: Exception) {
                Log.e(TAG, "Tunnel start failed", e)
                _connectionState.emit(ConnectionState.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun stop() {
        scope.launch {
            stopTunnel()
            _connectionState.emit(ConnectionState.Disconnected)
            _speedStats.emit(SpeedStats(0, 0, 0, 0))
        }
    }

    private suspend fun startXrayTunnel(config: TunnelConfig) {
        val xrayConfig = config.xrayConfig ?: buildXrayConfig(config.server)
        XrayLib.start(xrayConfig, tunFd?.fd)
    }

    private suspend fun startPsiphonTunnel(config: TunnelConfig) {
        val psiphonConfig = config.psiphonConfig ?: PsiphonConfig(
            serverHost = config.server.host,
            serverPort = config.server.port,
            authPassword = config.server.uuid ?: ""
        )
        PsiphonLib.start(psiphonConfig, tunFd?.fd)
    }

    private suspend fun startUdpTunnel(config: TunnelConfig) {
        UdpTunnelLib.start(
            host = config.server.host,
            port = config.server.port,
            fd = tunFd?.fd
        )
    }

    private suspend fun startDnsTunnel(config: TunnelConfig) {
        DnsTunnelLib.start(
            host = config.server.host,
            port = config.server.port,
            fd = tunFd?.fd
        )
    }

    private suspend fun startHttpSshTunnel(config: TunnelConfig) {
        HttpTunnelLib.start(
            host = config.server.host,
            port = config.server.port,
            payload = config.customPayload ?: config.server.payload,
            fd = tunFd?.fd
        )
    }

    private fun startTunnel() {
        // Native tunnel start - implemented via JNI
    }

    private fun stopTunnel() {
        XrayLib.stop()
        PsiphonLib.stop()
        UdpTunnelLib.stop()
        DnsTunnelLib.stop()
        HttpTunnelLib.stop()

        tunFd?.close()
        tunFd = null
    }

    private fun startSpeedMonitor() {
        scope.launch {
            while (_connectionState.value is ConnectionState.Connected) {
                val stats = getTunnelStats()
                _speedStats.emit(stats)
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun getTunnelStats(): SpeedStats {
        // Read byte counters from TUN interface
        val (bytesIn, bytesOut) = readTunCounters()
        return SpeedStats(
            bytesIn = bytesIn,
            bytesOut = bytesOut,
            speedIn = calculateSpeed(bytesIn),
            speedOut = calculateSpeed(bytesOut)
        )
    }

    private var lastBytesIn = 0L
    private var lastBytesOut = 0L
    private var lastTime = 0L

    private fun calculateSpeed(currentBytes: Long): Long {
        val now = System.currentTimeMillis()
        val delta = if (lastTime > 0) now - lastTime else 1000
        val speed = (currentBytes - lastBytesIn) * 1000 / delta.coerceAtLeast(1)
        lastBytesIn = currentBytes
        lastTime = now
        return speed
    }

    private fun readTunCounters(): Pair<Long, Long> {
        // Read from /proc/net/dev or via JNI
        return Pair(0, 0)
    }

    fun setTunFd(fd: ParcelFileDescriptor) {
        tunFd = fd
    }

    private fun buildXrayConfig(server: com.bdnet.vpn.data.model.Server): String {
        return """
        {
            "inbounds": [{
                "port": 10808,
                "protocol": "socks",
                "settings": {
                    "auth": "noauth",
                    "udp": true,
                    "userLevel": 8
                },
                "sniffing": {
                    "enabled": true,
                    "destOverride": ["http", "tls"]
                }
            }],
            "outbounds": [{
                "protocol": "${getOutboundProtocol(server.protocol)}",
                "settings": {
                    "vnext": [{
                        "address": "${server.host}",
                        "port": ${server.port},
                        "users": [{
                            "id": "${server.uuid ?: ""}",
                            "alterId": 0,
                            "security": "auto",
                            "level": 8
                        }]
                    }]
                },
                "streamSettings": {
                    "network": "${server.network ?: "tcp"}",
                    "security": "${server.security ?: "none"}",
                    "tlsSettings": {
                        "serverName": "${server.sni ?: server.host}",
                        "alpn": ["http/2", "h2"]
                    }
                },
                "mux": {
                    "enabled": true,
                    "concurrency": 8
                }
            }]
        }
        """.trimIndent()
    }

    private fun getOutboundProtocol(protocol: String): String {
        return when (protocol) {
            "vless" -> "vless"
            "trojan" -> "trojan"
            "shadowsocks" -> "shadowsocks"
            else -> "vmess"
        }
    }

    companion object {
        private const val TAG = "TunnelManager"

        @Volatile
        private var instance: TunnelManager? = null

        fun getInstance(): TunnelManager {
            return instance ?: synchronized(this) {
                val newInstance = TunnelManager()
                instance = newInstance
                newInstance
            }
        }
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class SpeedStats(
    val bytesIn: Long,
    val bytesOut: Long,
    val speedIn: Long,
    val speedOut: Long
) {
    companion object {
        val ZERO = SpeedStats(0, 0, 0, 0)
    }
}
