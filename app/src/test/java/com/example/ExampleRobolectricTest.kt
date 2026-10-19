package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.crypto.CryptoManager
import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

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
}
