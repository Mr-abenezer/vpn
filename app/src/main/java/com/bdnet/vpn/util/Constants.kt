package com.bdnet.vpn.util

object Constants {
    // Notification Channels
    const val CHANNEL_VPN_STATUS = "vpn_status"
    const val CHANNEL_VPN_UPDATES = "vpn_updates"
    const val CHANNEL_VPN_ALERTS = "vpn_alerts"

    // Notification IDs
    const val NOTIFICATION_VPN_ACTIVE = 1001
    const val NOTIFICATION_VPN_ERROR = 1002
    const val NOTIFICATION_UPDATE_AVAILABLE = 1003

    // SharedPreferences / DataStore Keys
    const val PREF_LAST_SERVER_ID = "last_server_id"
    const val PREF_AUTO_CONNECT = "auto_connect"
    const val PREF_SPLIT_TUNNEL_ENABLED = "split_tunnel_enabled"
    const val PREF_SPLIT_TUNNEL_MODE = "split_tunnel_mode"
    const val PREF_SELECTED_APPS = "selected_apps"
    const val PREF_CUSTOM_DNS_ENABLED = "custom_dns_enabled"
    const val PREF_DNS_PRIMARY = "dns_primary"
    const val PREF_DNS_SECONDARY = "dns_secondary"
    const val PREF_KILL_SWITCH = "kill_switch"
    const val PREF_NOTIFICATION_ENABLED = "notification_enabled"
    const val PREF_THEME = "theme"
    const val PREF_LANGUAGE = "language"

    // VPN Defaults
    const val DEFAULT_DNS_PRIMARY = "8.8.8.8"
    const val DEFAULT_DNS_SECONDARY = "8.8.4.4"
    const val DEFAULT_VPN_IP = "10.0.0.1"
    const val DEFAULT_VPN_SUBNET = 24
    const val VPN_ROUTE = "0.0.0.0"
    const val VPN_ROUTE_PREFIX = 0

    // Protocols
    const val PROTOCOL_SSH = "ssh"
    const val PROTOCOL_SSL = "ssl"
    const val PROTOCOL_HTTP = "http"
    const val PROTOCOL_V2RAY = "v2ray"
    const val PROTOCOL_VMESS = "vmess"
    const val PROTOCOL_VLESS = "vless"
    const val PROTOCOL_XTLS = "xtls"
    const val PROTOCOL_TROJAN = "trojan"
    const val PROTOCOL_SHADOWSOCKS = "shadowsocks"
    const val PROTOCOL_PSIHON = "psiphon"
    const val PROTOCOL_UDP = "udp"
    const val PROTOCOL_DNS_TUNNEL = "dns_tunnel"
    const val PROTOCOL_SOCKS5 = "socks5"

    // Split Tunnel Modes
    const val SPLIT_MODE_INCLUDE = "include"
    const val SPLIT_MODE_EXCLUDE = "exclude"

    // Firebase Collections
    const val FIRESTORE_COLLECTION_SERVERS = "servers"
    const val FIRESTORE_COLLECTION_ANNOUNCEMENTS = "announcements"
    const val FIRESTORE_COLLECTION_APP_CONFIG = "app_config"

    // Firebase Topics
    const val FCM_TOPIC_ALL_USERS = "all_users"
    const val FCM_TOPIC_BD_USERS = "bd_users"

    // API Endpoints
    const val API_IP_INFO = "https://ipinfo.io/json"
    const val API_IP_API = "https://ip-api.com/json/"

    // Payload Variables
    const val PAYLOAD_VAR_HOST = "[host]"
    const val PAYLOAD_VAR_PORT = "[port]"
    const val PAYLOAD_VAR_METHOD = "[method]"
    const val PAYLOAD_VAR_PATH = "[path]"

    // Broadcast Actions
    const val ACTION_SERVICE_STOP = "com.bdnet.vpn.ACTION_SERVICE_STOP"
    const val ACTION_SERVICE_RESTART = "com.bdnet.vpn.ACTION_SERVICE_RESTART"
    const val ACTION_CONNECTION_STATUS = "com.bdnet.vpn.ACTION_CONNECTION_STATUS"
    const val ACTION_SPEED_UPDATE = "com.bdnet.vpn.ACTION_SPEED_UPDATE"

    // Broadcast Extras
    const val EXTRA_STATUS = "status"
    const val EXTRA_BYTES_IN = "bytes_in"
    const val EXTRA_BYTES_OUT = "bytes_out"
    const val EXTRA_DURATION = "duration"
    const val EXTRA_SPEED_IN = "speed_in"
    const val EXTRA_SPEED_OUT = "speed_out"
    const val EXTRA_IP_ADDRESS = "ip_address"

    // Connection Status
    const val STATUS_DISCONNECTED = 0
    const val STATUS_CONNECTING = 1
    const val STATUS_CONNECTED = 2
    const val STATUS_ERROR = 3

    // File Extensions
    const val FILE_EXTENSION_CONFIG = ".vpn"
    const val FILE_EXTENSION_JSON = ".json"

    // QR Code
    const val QR_CODE_SIZE = 512

    // Animation Durations
    const val ANIMATION_SPLASH_DURATION = 2000L
    const val ANIMATION_CONNECT_DURATION = 500L

    // Ad Unit IDs (Replace with actual IDs)
    const val ADMOB_BANNER_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
    const val ADMOB_INTERSTITIAL_UNIT_ID = "ca-app-pub-ZZZZZZZZZZZZZZZZ/AAAAAAAAAA"

    // Update Check
    const val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
}
