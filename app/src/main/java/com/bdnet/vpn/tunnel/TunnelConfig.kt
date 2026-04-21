package com.bdnet.vpn.tunnel

import com.bdnet.vpn.data.model.Server

data class TunnelConfig(
    val server: Server,
    val dnsServers: List<String> = listOf("8.8.8.8", "8.8.4.4"),
    val routes: List<Route> = listOf(Route("0.0.0.0", 0)),
    val bypassApps: List<String> = emptyList(),
    val includeApps: List<String> = emptyList(),
    val mtu: Int = 1500,
    val sessionName: String = "BD NET VPN",
    val useDnsOverTls: Boolean = false,
    val useDnsOverHttps: Boolean = false,
    val customPayload: String? = null,
    val xrayConfig: String? = null,
    val psiphonConfig: PsiphonConfig? = null
) {
    data class Route(val address: String, val prefix: Int)
}

data class PsiphonConfig(
    val serverHost: String,
    val serverPort: Int,
    val authPassword: String,
    val region: String = "",
    val proxyProtocol: String = "unfronted",
    val tunnelProtocol: String = "unfronted",
    val useObfuscatedServerList: Boolean = true
)

data class XrayConfig(
    val vlessConfig: String? = null,
    val vmessConfig: String? = null,
    val trojanConfig: String? = null,
    val shadowsocksConfig: String? = null,
    val realityConfig: String? = null
)
