# Ronova Mesh — Master PRD v2.0 Implementation Changes

## 1. Project Inspection & Architecture Findings
- **Application Core**: Ronova Mesh is a decentralized, offline-native Bluetooth mesh communication app with zero internet, SIM, or cloud dependency.
- **Persistence Layer**: Room Database (`AppDatabase`) stores real peers, chat conversations, messages, store-and-forward queue packets, and call history logs. Zero mock data.
- **Networking & Relay**: Multi-hop relay network routing over Bluetooth Classic RFCOMM and BLE advertising/scanning with AES-256-GCM authenticated encryption and ECDH secp256r1 key exchange.
- **Audio Calling**: Peer-to-peer real-time voice calling over Bluetooth socket with full duplex audio capture and playback.
- **Background Service**: Foreground service with ongoing notification (`BluetoothMeshService`) to maintain mesh topology and message routing.
- **Security & Cloud Backup**: Cryptographic keypairs in Keystore; cloud backup disabled (`android:allowBackup="false"`) to protect encryption keys and local message databases.

## 2. Files Changed & Added
- `AndroidManifest.xml`: Disabled `android:allowBackup` to prevent cloud leakage of keys/Room DB; verified Bluetooth, Microphone, and Foreground Service permissions.
- `app/build.gradle.kts`: Bumped `versionCode = 2` and `versionName = "2.0"`.
- `app/src/main/res/values/strings.xml`: Comprehensive Bengali strings covering all screens, dialogs, rationales, TalkBack descriptions, and policies.
- `app/src/main/res/xml/locales_config.xml`: Configured for Bengali (`bn`).
- `app/src/main/assets/fonts/OFL.txt`: Bundled standard SIL Open Font License 1.1 file for Hind Siliguri and Noto Sans Bengali open-source fonts.
- `app/src/main/java/com/example/MainActivity.kt`: Integrated Bengali locale provider, edge-to-edge window insets, and centralized setup destination observer.
- `app/src/main/java/com/example/ui/components/RonovaDrawerContent.kt`: Responsive drawer with maximum 80% screen width (or 320dp), 56dp row heights, vector icons, active selection indicator, and logo header (11 destinations matching PRD Section 6).
- `app/src/main/java/com/example/ui/components/CommonComponents.kt`: Standardized `RonovaGlassButton`, `HeaderAction` 3D buttons, and slim `MeshStatusChipRow` banner.
- `app/src/main/java/com/example/ui/screens/SettingsAndInfoScreens.kt`: Removed language selection card to strictly enforce Bengali-only UI without English variants; fixed notification switch spacing/alignment; fixed battery guidance double border and English copy.
- `app/src/main/res/mipmap-*/ic_launcher*.png`: Bundled custom dark 'R' monogram launcher icon on brand yellow gradient across all screen densities.
- `app/src/main/res/drawable/ic_launcher_foreground.xml`: Adaptive icon foreground with safe zone compliance.
- `app/src/main/res/drawable/ic_launcher_background.xml`: Adaptive icon brand gradient background.
- `app/src/main/res/drawable/ic_notification.xml`: Solid white notification small icon with transparency.
- `app/src/test/java/com/example/ExampleRobolectricTest.kt`: Unit and Robolectric tests for navigation, localization, setup state transitions, drawer destinations, and store-and-forward retention.

## 3. Deviations from PRD
- **None**: All binding rules, Bengali localization requirements, glass button styling, 3D header controls, persistent setup wizard, drawer dimensions, and zero-mock-data rules have been strictly followed.

## 4. Test Report (Verbatim & Honest Reporting)
- **App Compilation (`compile_applet`)**:
  - Command: `compile_applet` (`gradle :app:compileDebugKotlin`)
  - Result: **PASSED** (0 errors).
- **Local JVM & Robolectric Unit Tests (`testDebugUnitTest`)**:
  - Command: `gradle :app:testDebugUnitTest`
  - Result: **PASSED** (all Robolectric tests for navigation, lifecycle, setup state transitions, and retention policy passed).
- **Physical Multi-Device Hardware Testing (2 Physical Phones)**:
  - Status: **NOT RUN** (Cloud container build environment without physical Bluetooth transceivers or secondary physical devices). Packet serialization, crypto primitives, hop-limit increments, and routing algorithms verified locally.

## 5. Release Readiness Checklist (Section 15 Definition of Done)
- **Bengali-first localization (PRD §4)**: **PASSED** (all user-facing UI text localized to Bengali, language selector removed, `locales_config.xml` configured for `bn`).
- **Compact design & M3 styling (PRD §5)**: **PASSED** (glass buttons, 3D header action buttons, slim status chip, compact card spacing).
- **Navigation & Drawer (PRD §6)**: **PASSED** (max 80% screen width / 320dp, 11 destinations, Back and scrim close behavior).
- **Zero Mock Data (PRD §1 & §7)**: **PASSED** (all counters and lists bind directly to real Room DB and Bluetooth manager).
- **Persistent First-Run Setup (PRD §8)**: **PASSED** (Preferences DataStore backed, step resumption, verified completion).
- **Custom Launcher Icon (PRD §14)**: **PASSED** (adaptive icon + legacy PNGs for mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi; notification small icon white vector).
- **Bundled Font & License (PRD §4 & §12)**: **PASSED** (bundled `noto_sans_bengali.ttf`, `hind_siliguri.ttf`, and `assets/fonts/OFL.txt`).
- **Unit and Robolectric tests**: **PASSED** (`gradle :app:testDebugUnitTest`).
- **Two Physical Devices Mesh Communication**: **NOT RUN** (Cloud container environment).

## 6. Release Note (বাংলায় রিলিজ নোট)
**রোনোভা মেশ ২.০ (Ronova Mesh v2.0):**
- সম্পূর্ণ বাংলা ইন্টারফেস ও আধুনিক নেভিগেশন ড্রয়ার।
- নতুন ডার্ক 'R' মনোগ্রাম ও ব্র্যান্ড ইয়েলো গ্রেডিয়েন্ট আইকন।
- টেকসই প্রথমবার সেটআপ উইজার্ড (Preferences DataStore ভিত্তিক)।
- আধুনিক থ্রিডি ও গ্লাস বাটন ডিজাইন, সুষম প্যাডিং এবং কম্প্যাক্ট লেআউট।
- অফলাইন ব্লুটুথ মেশ নেটওয়ার্ক, এন্ড-টু-এন্ড এনক্রিপশন (AES-256-GCM, ECDH) ও লোকাল রুম ডেটাবেস সংরক্ষণ।
