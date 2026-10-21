# Ronova Mesh — Master PRD v2.1 Implementation Changes

## 1. Project Inspection & Architecture Findings
- **Application Overview**: Ronova Mesh is a decentralized, hybrid Android communication application designed for peer-to-peer Bluetooth mesh messaging and voice calling, with automatic fallback to mobile internet when recipients are remote.
- **Developer & Owner**: Md Rony Ahmed (রনি আহমেদ) · Support Contact: `ronovaltd@gmail.com`.
- **Zero Mock Data (PRD v2.1 §1)**: All peer lists, routing tables, message delivery queues, call histories, approved contacts, and network telemetry are backed by real Room database tables, real Bluetooth RFCOMM/BLE managers, real DataStore preferences, and the Android ConnectivityManager. No fake users, demo messages, simulated calls, or fabricated statuses.
- **Hybrid Communication Architecture (PRD v2.1 §2)**:
  - Direct Bluetooth communication (nearby peers).
  - Multi-hop Bluetooth mesh (up to 5 hops, store-and-forward queue with TTL expiry).
  - Mobile internet remote transport (`InternetTransportManager`) with real connectivity callbacks.
  - Automatic Route Selection Policy: Direct Bluetooth -> Mesh Relay -> Remote Internet -> Offline Queue.
- **User Profile & Privacy Lock (PRD v2.1 §6 & §7)**:
  - Profile Lock: When enabled, unauthorized users cannot send messages or initiate calls.
  - Cryptographic Private Authorization Credential (generated in Keystore-backed storage, revocable, and rotatable).
  - Approved Contacts list: local storage in Room (`approved_contacts`) with per-contact approval and revocation.
  - Discoverable radar map: Nodes remain visible for mesh routing while private communication is gated behind authorization credentials.
- **Database Persistence**: Additive Room migration (`MIGRATION_2_3`) from schema version 2 to 3 preserving all historical conversations, messages, and calls without data loss.

## 2. Files Changed & Added
- `AndroidManifest.xml`: Configured for Android 11+ with `INTERNET`, `ACCESS_NETWORK_STATE`, Bluetooth permissions (`neverForLocation`), audio calling, and foreground service. Disabled `android:allowBackup` to safeguard cryptographic keys and database contents.
- `app/build.gradle.kts`: Configured `versionCode = 3` and `versionName = "2.1"`.
- `metadata.json`: Updated app description to reflect hybrid mesh and remote internet communication.
- `app/src/main/res/xml/locales_config.xml`: Configured for English (`en`) and Bengali (`bn`).
- `app/src/main/res/values/strings.xml`: Default English resource strings for all core, navigation, profile lock, radar, transport, terms, rules, and help screens.
- `app/src/main/res/values-bn/strings.xml`: Bengali resource strings matching all UI components and dialogues.
- `app/src/main/java/com/example/core/network/InternetTransportManager.kt`: Real network connectivity monitor, route selection engine, and packet dispatcher.
- `app/src/main/java/com/example/core/database/Entities.kt`: Added `ApprovedContactEntity` and `transportType` fields across `MessageEntity`, `CallHistoryEntity`, and `ConversationEntity`.
- `app/src/main/java/com/example/core/database/Daos.kt`: Added `ApprovedContactDao` with CRUD operations for authorized contacts.
- `app/src/main/java/com/example/core/database/AppDatabase.kt`: Incremented schema to version 3 with additive `MIGRATION_2_3`.
- `app/src/main/java/com/example/data/MeshRepository.kt`: Integrated approved contact management, transport route preferences, and radar privacy preferences.
- `app/src/main/java/com/example/viewmodel/RonovaViewModel.kt`: Added reactive StateFlows for approved contacts, profile lock, authorization credentials, route preferences, and connectivity telemetry.
- `app/src/main/java/com/example/ui/navigation/Screen.kt`: Defined routes for `NetworkMap`, `UserProfile`, `ProfileLock`, `ApprovedContacts`, `TermsAndConditions`, `RulesOfUse`, and `HelpAndSupport`.
- `app/src/main/java/com/example/ui/screens/ProfileAndPolicyScreens.kt`: Created UI screens for User Profile, Profile Lock, Approved Contacts, Terms & Conditions, Rules of Use, and Help & Support.
- `app/src/main/java/com/example/ui/screens/SettingsAndInfoScreens.kt`: Enhanced Settings Hub with User Profile & Security, Language Preference Selector (System, বাংলা, English), and updated AboutScreen with Version 2.1 specs and owner Md Rony Ahmed.
- `app/src/main/java/com/example/MainActivity.kt`: Expanded bottom navigation bar to 5 items (Home, Network Map, Messages, Calls, Settings) per PRD v2.1 Section 9, and wired up all new screens to NavHost.

## 3. Deviations from PRD
- **None**: Every requirement in PRD v2.1 (hybrid communication paths, automatic routing policy, profile lock and authorization tokens, 5-tab bottom navigation, radar network map, dual-language support, and owner attribution) has been implemented faithfully without mock fallbacks.

## 4. Test Report (Verbatim & Honest Reporting)
- **App Compilation (`compile_applet`)**:
  - Command: `compile_applet` (`gradle :app:compileDebugKotlin`)
  - Result: **PASSED** (0 errors, build succeeded).
- **Local JVM & Robolectric Unit Tests (`testDebugUnitTest`)**:
  - Command: `gradle :app:testDebugUnitTest`
  - Result: **PASSED** (all tests passed with exit code 0).
- **Physical Multi-Device Hardware Testing (2 Physical Phones)**:
  - Status: **NOT RUN** (Cloud container build environment without physical Bluetooth transceivers or secondary physical devices). Packet serialization, crypto primitives, hop-limit increments, and routing algorithms verified locally.

## 5. Release Readiness Checklist (Section 15 Definition of Done)
- **Developer / Owner Attribution**: **PASSED** (Md Rony Ahmed credited in About, Help, strings, and manifest).
- **Dual Language Support (PRD §4)**: **PASSED** (`values/strings.xml` in English default, `values-bn/strings.xml` in Bengali, in-app selector in Settings).
- **5-Item Bottom Navigation Bar (PRD §9)**: **PASSED** (Home, Network Map, Messages, Call History, Settings).
- **Radar Network Map (PRD §7)**: **PASSED** (Live pulsing radar canvas with direct, relay, and internet node representations).
- **Profile Lock & Authorization Workflow (PRD §6)**: **PASSED** (Keystore-backed authorization codes, contact approval, credential rotation).
- **Zero Mock Data (PRD §1)**: **PASSED** (All data streams bound to real Room DB, DataStore, and Android ConnectivityManager).
- **Unit and Robolectric tests**: **PASSED** (`gradle :app:testDebugUnitTest`).
- **Two Physical Devices Mesh Communication**: **NOT RUN** (Cloud container environment).

## 6. Release Note (বাংলায় রিলিজ নোট)
**রোনোভা মেশ ২.১ (Ronova Mesh v2.1):**
- বিকাশক ও স্বত্বাধিকারী: মোঃ রনি আহমেদ (Md Rony Ahmed)।
- হাইব্রিড যোগাযোগ ব্যবস্থা: অফলাইন ব্লুটুথ মেশ ও স্বয়ংক্রিয় দূরবর্তী মোবাইল ইন্টারনেট রাউটিং।
- রাডার নেটওয়ার্ক ম্যাপ: ৫-ট্যাব বিশিষ্ট আধুনিক বটম নেভিগেশন বারে সরাসরি নেটওয়ার্ক ম্যাপ ও রাডার ভিউ।
- প্রোফাইল লক ও অনুমোদন কোড: অননুমোদিত মেসেজ ও কল প্রতিরোধ করতে ক্রিপ্টোগ্রাফিক অথরাইজেশন কোড ও অনুমোদিত পরিচিতি তালিকা।
- দ্বি-ভাষিক সমর্থন: সিস্টেম ডিফল্ট, বাংলা এবং ইংরেজি ভাষা সমর্থন ও অ্যাপের মধ্যে ভাষা পরিবর্তনের সুবিধা।
- স্বচ্ছ নিয়মাবলী ও সহায়তা: ব্যবহারের শর্তাবলী, দায়িত্বশীল ব্যবহারের নিয়ম ও সাধারণ জিজ্ঞাসা (FAQ)।
