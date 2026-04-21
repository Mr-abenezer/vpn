# BD NET VPN - Android VPN Application

A complete Android VPN application clone of BD NET VPN, featuring multiple VPN protocols, carrier-specific configurations, and HTTP injection capabilities.

## Features

- **Multiple VPN Protocols**: V2Ray, VMess, VLESS, XTLS, Trojan, Shadowsocks, Psiphon, SSH, SSL, HTTP, UDP Tunnel, DNS Tunnel
- **HTTP Injection**: Custom payload builder for carrier-specific free data loopholes
- **Split Tunneling**: Per-app VPN routing
- **VPN Hotspot Sharing**: Share VPN connection via WiFi hotspot
- **IP Hunter**: IP geolocation lookup tool
- **Real-time Stats**: Live upload/download speed monitoring
- **Firebase Integration**: Server updates, push notifications, analytics
- **AdMob Integration**: Banner and interstitial ads

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM
- **Min SDK**: 23 (Android 6.0)
- **Target SDK**: 34 (Android 14)
- **Database**: Room (SQLite)
- **Settings**: DataStore Preferences
- **Backend**: Firebase (Firestore, FCM, Analytics)
- **Ads**: Google AdMob

## Project Structure

```
app/src/main/java/com/bdnet/vpn/
├── VPNApplication.kt          # Application class
├── data/
│   ├── local/                  # Room database
│   │   ├── dao/               # Data Access Objects
│   │   ├── entity/            # Database entities
│   │   ├── AppDatabase.kt
│   │   └── Converters.kt
│   ├── model/                  # Domain models
│   └── repository/             # Data repositories
├── service/                    # VPN services
│   ├── TunnelVpnService.kt
│   ├── PsiphonVpnService.kt
│   └── ...
├── tunnel/                     # Tunnel engine
│   ├── TunnelManager.kt
│   ├── TunnelConfig.kt
│   └── Native libs (XrayLib, PsiphonLib, etc.)
├── ui/
│   ├── activities/            # All activities
│   ├── adapters/              # RecyclerView adapters
│   └── view/                  # Custom views
└── util/                       # Utilities & constants
```

## Setup Instructions

### Prerequisites

1. Android Studio Hedgehog (2023.1.1) or newer
2. JDK 17
3. Android SDK with API 34

### Configuration Steps

1. **Clone the project** to your local machine

2. **Setup Firebase**:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app with package name `com.bdnet.vpn`
   - Download `google-services.json` and replace the placeholder in `app/`
   - Enable Firestore, Firebase Messaging, and Analytics

3. **Setup AdMob**:
   - Create AdMob account at [AdMob](https://admob.google.com)
   - Create banner and interstitial ad units
   - Update ad unit IDs in `Constants.kt` and `AndroidManifest.xml`

4. **Add Native Libraries**:
   - Place compiled `.so` files in `app/src/main/jniLibs/arm64-v8a/`:
     - `libxray.so` - XRay core engine
     - `libgojni.so` - Go JNI bridge
     - `libudpq.so` - UDP tunnel
     - `libxdnstt.so` - DNS tunnel
     - `libfwpuncher.so` - Firewall puncher
     - `libtunpsi.so` - Psiphon tunnel
     - `libtun2socks.so` - TUN to SOCKS

5. **Add App Icons**:
   - Add launcher icons to `mipmap-*` folders
   - Add `ic_app_icon.png` to `drawable/`
   - Add country flags to `drawable/` (flag_XX.png format)

6. **Build the project**:
   ```bash
   ./gradlew assembleDebug
   ```

## Building Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated at `app/build/outputs/apk/release/`

## Key Components

### VPN Service Flow

1. User taps connect button
2. `VPNClient` sends intent to `TunnelVpnService`
3. Service builds TUN interface via `VpnService.Builder`
4. Native library (XRay, Psiphon, etc.) is started via JNI
5. Traffic flows: Apps → TUN → Native lib → Remote server → Internet

### Protocol Support

| Protocol | Library | Config |
|----------|---------|--------|
| V2Ray/VMess | libxray.so | JSON config |
| VLESS | libxray.so | JSON config |
| Trojan | libxray.so | JSON config |
| Shadowsocks | libxray.so | JSON config |
| Psiphon | libtunpsi.so | PsiphonConfig |
| UDP Tunnel | libudpq.so | Host/Port |
| DNS Tunnel | libxdnstt.so | Host/Port |
| SSH/SSL/HTTP | libfwpuncher.so | HTTP Payload |

## License

This project is for educational purposes only.

## Credits

- Original app: BD NET VPN by SulitNet Solutions
- XRay Core: https://github.com/XTLS/Xray-core
- Psiphon: https://github.com/Psiphon-Labs/psiphon
- tun2socks: https://github.com/eycorsican/go-tun2socks
