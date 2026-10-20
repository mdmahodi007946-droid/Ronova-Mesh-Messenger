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
    fun `locale helper persistence and language switching test`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        com.example.core.locale.LocaleHelper.setLanguage(context, "bn")
        assertEquals("bn", com.example.core.locale.LocaleHelper.getPersistedLanguage(context))

        com.example.core.locale.LocaleHelper.setLanguage(context, "en")
        assertEquals("en", com.example.core.locale.LocaleHelper.getPersistedLanguage(context))
    }

    @Test
    fun `localized context resolves English and Bengali strings`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val enContext = com.example.core.locale.LocaleHelper.getLocalizedContext(context, "en")
        assertEquals("Home", enContext.getString(R.string.nav_home))
        assertEquals("Messages", enContext.getString(R.string.nav_chats))
        assertEquals("Call History", enContext.getString(R.string.nav_calls))
        assertEquals("Settings", enContext.getString(R.string.nav_settings))
        assertEquals("Connected Peers", enContext.getString(R.string.drawer_connected_peers))

        val bnContext = com.example.core.locale.LocaleHelper.getLocalizedContext(context, "bn")
        assertEquals("হোম", bnContext.getString(R.string.nav_home))
        assertEquals("বার্তা", bnContext.getString(R.string.nav_chats))
        assertEquals("কলের ইতিহাস", bnContext.getString(R.string.nav_calls))
        assertEquals("সেটিংস", bnContext.getString(R.string.nav_settings))
        assertEquals("সংযুক্ত নোডসমূহ", bnContext.getString(R.string.drawer_connected_peers))
    }

    @Test
    fun `four primary bottom navigation destinations are defined`() {
        val expectedBottomRoutes = listOf(
            com.example.ui.navigation.Screen.Home.route,
            com.example.ui.navigation.Screen.Settings.route,
            com.example.ui.navigation.Screen.Chats.route,
            com.example.ui.navigation.Screen.CallHistory.route
        )
        assertEquals(4, expectedBottomRoutes.size)
        assertEquals("home", expectedBottomRoutes[0])
        assertEquals("settings", expectedBottomRoutes[1])
        assertEquals("chats", expectedBottomRoutes[2])
        assertEquals("call_history", expectedBottomRoutes[3])
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
            com.example.ui.navigation.Screen.BatteryGuidance.route,
            com.example.ui.navigation.Screen.Permissions.route,
            com.example.ui.navigation.Screen.SecuritySettings.route,
            com.example.ui.navigation.Screen.Settings.route,
            com.example.ui.navigation.Screen.About.route
        )
        assertEquals(11, drawerRoutes.size)
    }
}
