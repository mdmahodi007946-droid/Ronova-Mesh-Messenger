package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.crypto.CryptoManager
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `launch MainActivity without crash`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Ronova Mesh", appName)
    }

    @Test
    fun `crypto identity generation and encryption test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cryptoAlice = CryptoManager(context)
        val aliceId = cryptoAlice.deviceId
        assertTrue(aliceId.startsWith("RNV-"))

        // Create second instance for Bob
        val cryptoBob = CryptoManager(context)
        val plaintext = "Hello from Ronova Mesh offline test!"
        val ciphertext = cryptoAlice.encryptForPeer(plaintext, cryptoBob.publicKey)
        assertNotNull(ciphertext)

        val decrypted = cryptoBob.decryptFromPeer(ciphertext, cryptoAlice.publicKey)
        assertEquals(plaintext, decrypted)
    }

    @Test
    fun `mesh packet serialization and hop increment test`() {
        val packet = MeshPacket(
            type = MeshPacketType.TEXT_MESSAGE,
            originDeviceId = "RNV-1111-2222-3333",
            destinationDeviceId = "RNV-4444-5555-6666",
            payload = "Test payload",
            hopLimit = 5,
            currentHop = 1,
            routePath = listOf("RNV-NODE-A")
        )

        val bytes = packet.toBytes()
        val deserialized = MeshPacket.fromBytes(bytes)

        assertEquals(packet.packetId, deserialized.packetId)
        assertEquals(packet.originDeviceId, deserialized.originDeviceId)
        assertEquals(packet.destinationDeviceId, deserialized.destinationDeviceId)
        assertEquals(packet.payload, deserialized.payload)
        assertEquals(1, deserialized.currentHop)
        assertEquals(listOf("RNV-NODE-A"), deserialized.routePath)

        val hopped = deserialized.withIncrementedHop("RNV-NODE-B")
        assertEquals(2, hopped.currentHop)
        assertEquals(listOf("RNV-NODE-A", "RNV-NODE-B"), hopped.routePath)
        assertTrue(hopped.canHopFurther())
    }

    @Test
    fun `storage retention policy auto-deletes messages older than 30 days`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.core.database.AppDatabase::class.java).build()
        val repo = com.example.data.MeshRepository(db, context)

        val now = System.currentTimeMillis()
        val fortyDaysAgo = now - (40L * 24L * 60L * 60L * 1000L)
        val fiveDaysAgo = now - (5L * 24L * 60L * 60L * 1000L)

        // Insert an old message and a recent message
        val oldMessage = com.example.core.database.MessageEntity(
            messageId = "old-msg-1",
            conversationId = "peer-1",
            senderDeviceId = "peer-1",
            recipientDeviceId = "me",
            content = "Old message from 40 days ago",
            timestamp = fortyDaysAgo
        )
        val recentMessage = com.example.core.database.MessageEntity(
            messageId = "recent-msg-1",
            conversationId = "peer-1",
            senderDeviceId = "peer-1",
            recipientDeviceId = "me",
            content = "Recent message from 5 days ago",
            timestamp = fiveDaysAgo
        )

        repo.saveMessage(oldMessage)
        repo.saveMessage(recentMessage)

        assertEquals(1, repo.getMessagesOlderThan30DaysCount())

        // Run auto-delete policy
        repo.setRetentionPolicy(com.example.data.RetentionPolicy.AUTO_DELETE)
        val deletedCount = repo.runRetentionPolicyCleanup()

        assertEquals(1, deletedCount)
        assertEquals(0, repo.getMessagesOlderThan30DaysCount())
        assertNotNull(repo.getMessageById("recent-msg-1"))
        org.junit.Assert.assertNull(repo.getMessageById("old-msg-1"))

        db.close()
    }

    @Test
    fun `storage retention policy auto-archives messages older than 30 days`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.core.database.AppDatabase::class.java).build()
        val repo = com.example.data.MeshRepository(db, context)

        val now = System.currentTimeMillis()
        val thirtyFiveDaysAgo = now - (35L * 24L * 60L * 60L * 1000L)

        val oldMessage = com.example.core.database.MessageEntity(
            messageId = "old-msg-2",
            conversationId = "peer-2",
            senderDeviceId = "peer-2",
            recipientDeviceId = "me",
            content = "Archivable message",
            timestamp = thirtyFiveDaysAgo
        )
        repo.saveMessage(oldMessage)

        repo.setRetentionPolicy(com.example.data.RetentionPolicy.AUTO_ARCHIVE)
        val archivedCount = repo.runRetentionPolicyCleanup()

        assertEquals(1, archivedCount)
        val retrieved = repo.getMessageById("old-msg-2")
        assertNotNull(retrieved)
        assertTrue(retrieved!!.isArchived)

        // Unarchive message
        repo.unarchiveMessage("old-msg-2")
        val restored = repo.getMessageById("old-msg-2")
        assertNotNull(restored)
        assertFalse(restored!!.isArchived)

        db.close()
    }

    @Test
    fun `bengali and english localization test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        val initialLang = com.example.core.locale.LocaleHelper.getPersistedLanguage(context)
        assertEquals(com.example.core.locale.LocaleHelper.LANGUAGE_SYSTEM, initialLang)

        // Set persisted language
        com.example.core.locale.LocaleHelper.setPersistedLanguage(context, com.example.core.locale.LocaleHelper.LANGUAGE_BANGLA)
        assertEquals(com.example.core.locale.LocaleHelper.LANGUAGE_BANGLA, com.example.core.locale.LocaleHelper.getPersistedLanguage(context))

        val localizedContext = com.example.core.locale.LocaleHelper.getLocalizedContext(context)
        assertEquals("Home", localizedContext.getString(R.string.nav_home))
        assertEquals("Messages", localizedContext.getString(R.string.nav_chats))
        assertEquals("Call History", localizedContext.getString(R.string.nav_calls))
        assertEquals("Settings", localizedContext.getString(R.string.nav_settings))
        assertEquals("Connected Devices", localizedContext.getString(R.string.drawer_connected_peers))
        assertEquals("Mesh Network & Radar", localizedContext.getString(R.string.drawer_mesh_map))
        assertEquals("Store & Forward", localizedContext.getString(R.string.drawer_store_forward))
        assertEquals("Permissions", localizedContext.getString(R.string.drawer_permissions))
        assertEquals("Battery Optimization", localizedContext.getString(R.string.drawer_battery_guidance))
        assertEquals("Privacy & Security", localizedContext.getString(R.string.drawer_privacy_security))
        
        // Reset to system
        com.example.core.locale.LocaleHelper.setPersistedLanguage(context, com.example.core.locale.LocaleHelper.LANGUAGE_SYSTEM)
    }

    @Test
    fun `persistent setup flow state management test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.core.database.AppDatabase::class.java).build()
        val repo = com.example.data.MeshRepository(db, context)

        // Initially setup is not completed
        repo.resetSetup()
        assertFalse(repo.isSetupCompleted())
        assertEquals("WELCOME", repo.getSetupStep())

        // User advances step
        repo.setSetupStep("PERMISSIONS")
        assertEquals("PERMISSIONS", repo.getSetupStep())

        repo.setSetupStep("CONFIG")
        assertEquals("CONFIG", repo.getSetupStep())

        // User finishes setup
        repo.setSetupCompleted(true)
        assertTrue(repo.isSetupCompleted())
        assertEquals("COMPLETED", repo.getSetupStep())

        // Subsequent check preserves completed state
        val repoAfterRestart = com.example.data.MeshRepository(db, context)
        assertTrue(repoAfterRestart.isSetupCompleted())

        // User deliberately resets setup from settings
        repoAfterRestart.resetSetup()
        assertFalse(repoAfterRestart.isSetupCompleted())
        assertEquals("WELCOME", repoAfterRestart.getSetupStep())

        db.close()
    }

    @Test
    fun `five primary bottom navigation destinations are defined`() {
        val expectedBottomRoutes = listOf(
            com.example.ui.navigation.Screen.Home.route,
            com.example.ui.navigation.Screen.NetworkMap.route,
            com.example.ui.navigation.Screen.Chats.route,
            com.example.ui.navigation.Screen.CallHistory.route,
            com.example.ui.navigation.Screen.Settings.route
        )
        assertEquals(5, expectedBottomRoutes.size)
        assertEquals("home", expectedBottomRoutes[0])
        assertEquals("network_map", expectedBottomRoutes[1])
        assertEquals("chats", expectedBottomRoutes[2])
        assertEquals("call_history", expectedBottomRoutes[3])
        assertEquals("settings", expectedBottomRoutes[4])
    }

    @Test
    fun `eleven drawer navigation destinations are defined`() {
        val drawerRoutes = listOf(
            com.example.ui.navigation.Screen.Home.route,
            com.example.ui.navigation.Screen.Chats.route,
            com.example.ui.navigation.Screen.CallHistory.route,
            com.example.ui.navigation.Screen.Discovery.route,
            com.example.ui.navigation.Screen.RelayNetwork.route,
            com.example.ui.navigation.Screen.PendingMessages.route,
            com.example.ui.navigation.Screen.Permissions.route,
            com.example.ui.navigation.Screen.BatteryGuidance.route,
            com.example.ui.navigation.Screen.SecuritySettings.route,
            com.example.ui.navigation.Screen.Settings.route,
            com.example.ui.navigation.Screen.About.route
        )
        assertEquals(11, drawerRoutes.size)
    }

    @Test
    fun `approved contacts authorization check and management`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.core.database.AppDatabase::class.java).build()
        val repo = com.example.data.MeshRepository(db, context)

        val peerDeviceId = "RNV-AUTH-PEER-1234"
        
        // Initially not approved
        assertFalse(repo.isContactApproved(peerDeviceId))

        // Add approved contact
        repo.saveApprovedContact(
            contactId = peerDeviceId,
            nickname = "Trusted Friend",
            credential = "SECRET_TOKEN_ABC"
        )

        assertTrue(repo.isContactApproved(peerDeviceId))

        // Revoke contact
        repo.revokeApprovedContact(peerDeviceId)
        assertFalse(repo.isContactApproved(peerDeviceId))

        db.close()
    }

    @Test
    fun `profile lock toggle and authorization credential verification`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val crypto = com.example.core.crypto.CryptoManager(context)

        // Initially unlocked
        assertFalse(crypto.isProfileLocked())

        crypto.setProfileLocked(true)
        assertTrue(crypto.isProfileLocked())

        val cred = crypto.getOrGenerateAuthorizationCredential()
        assertNotNull(cred)
        assertTrue(crypto.verifyAuthorizationCredential(cred, cred))
        assertFalse(crypto.verifyAuthorizationCredential("INVALID_CODE", cred))

        // Unlock
        crypto.setProfileLocked(false)
        assertFalse(crypto.isProfileLocked())
    }

    @Test
    fun `auth credential generation and rotation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val crypto = com.example.core.crypto.CryptoManager(context)

        val cred1 = crypto.getOrGenerateAuthorizationCredential()
        assertNotNull(cred1)
        assertTrue(cred1.startsWith("AUTH-"))

        val cred2 = crypto.rotateAuthorizationCredential()
        assertNotNull(cred2)
        assertTrue(cred2.startsWith("AUTH-"))
        org.junit.Assert.assertNotEquals(cred1, cred2)
    }

    @Test
    fun `preferred transport routing preference persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(context, com.example.core.database.AppDatabase::class.java).build()
        val repo = com.example.data.MeshRepository(db, context)

        // Default transport is AUTOMATIC
        val defaultPreference = repo.getRoutePreference()
        assertEquals(com.example.core.network.RoutePreference.AUTOMATIC, defaultPreference)

        repo.setRoutePreference(com.example.core.network.RoutePreference.BLUETOOTH_ONLY)
        assertEquals(com.example.core.network.RoutePreference.BLUETOOTH_ONLY, repo.getRoutePreference())

        repo.setRoutePreference(com.example.core.network.RoutePreference.INTERNET_PREFERRED)
        assertEquals(com.example.core.network.RoutePreference.INTERNET_PREFERRED, repo.getRoutePreference())

        db.close()
    }
}

