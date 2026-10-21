# Ronova Bluetooth Mesh Messenger

**Developer / Project Owner:** Md Rony Ahmed  
**Support Contact:** ronovaltd@gmail.com  
**Version:** 2.1 Production Specification  
**Primary Language:** Kotlin  
**UI Framework:** Jetpack Compose & Material 3  
**Target Platform:** Android 11+ (API 30+)  
**Primary Communication:** Bluetooth Mesh + Mobile Internet

---

## 1. Project Overview

Ronova Bluetooth Mesh Messenger is a native, offline Android communication application engineered to deliver sovereign, internet-independent messaging and voice communication directly between nearby compatible Android devices.

### Core Principles
1. **Zero Internet Required:** Does not require Wi-Fi, cellular mobile data, SIM cards, or central cloud servers.
2. **Multi-Hop Store-and-Forward Mesh:** Messages traverse intermediate Android nodes to reach recipients out of direct Bluetooth range.
3. **End-to-End Cryptography:** Elliptic Curve Diffie-Hellman (secp256r1) key exchange with AES-256-GCM authenticated encryption. Intermediary nodes forward encrypted payloads without access to message plaintext.
4. **Direct Bluetooth Voice Calling:** High-efficiency 16kHz audio capture (`AudioRecord`), IMA-ADPCM streaming over RFCOMM, and playback (`AudioTrack`).
5. **Decentralized Cryptographic Identity:** Identity is bound to public key SHA-256 fingerprints (`RNV-xxxx-xxxx-xxxx`) stored locally in Android KeyStore/SharedPreferences.

---

## 2. Architecture & Modular Package Structure

```
com.example
├── RonovaApplication.kt         # Application lifecycle & dependency singletons
├── MainActivity.kt              # Single-activity Jetpack Compose container & NavHost
├── core
│   ├── crypto
│   │   └── CryptoManager.kt     # EC secp256r1 keypair, ECDH, AES-256-GCM, HMAC-SHA256
│   ├── protocol
│   │   └── MeshPacket.kt        # Versioned mesh envelope & serialization
│   ├── database
│   │   ├── Entities.kt          # Room entities: Devices, Conversations, Messages, RelayQueue
│   │   ├── Daos.kt              # Reactive Flow DAOs
│   │   └── AppDatabase.kt       # Room SQLite database holder
│   ├── bluetooth
│   │   └── BluetoothMeshManager.kt # RFCOMM server/client, peer discovery, packet routing
│   └── audio
│       └── AudioCallManager.kt  # 16kHz AudioRecord/AudioTrack, IMA-ADPCM codec, call FSM
├── data
│   └── MeshRepository.kt        # Repository abstraction
├── service
│   └── BluetoothMeshService.kt  # Android Foreground Service for background mesh routing
├── ui
│   ├── theme                    # Premium pink-and-white design tokens (#EC4899, #FFF7FB)
│   ├── navigation               # Type-safe routing for all 27 PRD screens
│   ├── components               # Reusable top bars, stat cards, waveform visualizers
│   └── screens                  # 27 production screens
└── viewmodel
    └── RonovaViewModel.kt       # Reactive AndroidViewModel orchestrator
```

---

## 3. Implemented Screens (All 27 PRD Destinations)

1. **Splash Screen:** Branding, cryptographic identity initialization.
2. **First-Launch Welcome Screen:** Offline mission statement and core features.
3. **Privacy & Permissions Explanation:** Transparent justification of Bluetooth, Audio, and Notifications.
4. **Bluetooth Setup Screen:** Radio verification, activation status, and cryptographic ID preview.
5. **Nearby Device Discovery Screen:** Live RFCOMM scanning, signal RSSI, connect triggers.
6. **Device Connection & Identity Verification Screen:** Fingerprint comparison, contact renaming, trust/block controls.
7. **Home Dashboard:** Mesh status badge, quick stats, active peers, recent conversations.
8. **Conversation List Screen:** Message threads, unread counters, local search, new chat FAB.
9. **One-to-One Chat Screen:** Bubble thread, real delivery states (Queued, Sending, Relayed, Delivered), call shortcut.
10. **Message Delivery Details Screen:** Hop counts, route path array, timestamps, ciphertext metadata.
11. **Pending Messages Screen:** Store-and-forward queue with manual retry triggers.
12. **Nearby Relay Network Screen:** Real-time animated canvas mesh radar topology map.
13. **Relay Participation Settings:** Opt-in toggle, max hops slider (1–10), battery threshold, queue limits.
14. **Outgoing Voice Call Screen:** Audio signalling, ringing pulse animation, cancel action.
15. **Incoming Voice Call Screen:** Caller identity verification, accept and reject actions.
16. **Active Voice Call Screen:** Real-time audio waveform visualizer, mute, speaker, call timer, packet telemetry.
17. **Call History:** Past call records with durations and redial buttons.
18. **Trusted Devices:** Verified cryptographic contacts and trust revocation.
19. **Blocked Devices:** Blacklisted nodes unable to send messages or place calls.
20. **Security & Encryption Settings:** Public key fingerprint exporter, cryptographic architecture audit.
21. **Storage & Backup Settings:** Local SQLite database size inspection and cache cleanup.
22. **Battery & Background Guidance:** OEM battery optimization instructions (Samsung, Xiaomi, Pixel).
23. **Notification Settings:** Ringtone, vibration, and persistent service toggles.
24. **Privacy Policy & Open-Source Licenses:** Zero-cloud pledge, MIT/Apache 2.0 notices.
25. **Application Diagnostics:** Live RFCOMM packet counters (Sent, Received, Relayed, Dropped) & security logs.
26. **About Ronova Mesh:** Developer Md Rony Ahmed overview and official support contact (`ronovaltd@gmail.com`).
27. **Error & Recovery Screen:** Bluetooth server reset, queue flush, and EC keypair regeneration.
28. **User Profile Screen:** Custom nickname, radar visibility toggle, and device name privacy controls.
29. **Profile Lock Screen:** Private authorization code, contact authorization, and rotation.
30. **Approved Contacts Screen:** List and revocation of authorized contacts for locked profiles.
31. **Terms & Conditions Screen:** Legal agreements, software license, and acceptable usage.
32. **Rules of Use Screen:** Community rules, responsible mesh relaying, and safety guidelines.
33. **Help & Support Screen:** Frequently asked questions, troubleshooting, and direct support.

---

## 4. Physical Device Multi-Hop Testing Guide

To test the multi-hop routing protocol across physical Android phones without internet:

1. **Device Setup:**
   - Turn off Wi-Fi and Mobile Data on Phone A, Phone B, and Phone C.
   - Install the Ronova Mesh APK on all three devices.
2. **Topology Placement:**
   - Place **Phone A** in Room 1.
   - Place **Phone B** (Relay) in the hallway midway between Room 1 and Room 2.
   - Place **Phone C** in Room 2 (out of direct Bluetooth RFCOMM range from Phone A).
3. **Pairing & Discovery:**
   - Open Discovery on Phone A and connect to Phone B.
   - Open Discovery on Phone C and connect to Phone B.
4. **Multi-Hop Text Delivery:**
   - Send a message from Phone A addressed to Phone C's cryptographic ID (`RNV-xxxx...`).
   - Phone A marks state as `RELAYED` (Hop 1).
   - Phone B receives the encrypted packet, checks its routing table, increments current hop, and forwards to Phone C.
   - Phone C decrypts the message, displays the text bubble, and transmits an authenticated `ACK_DELIVERED` packet back through Phone B to Phone A.
   - Phone A updates the message checkmark to double-check `DELIVERED` (2 Hops).
