package com.example

import com.example.core.protocol.MeshPacket
import com.example.core.protocol.MeshPacketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun `packet serialization preserves all fields and signature`() {
        val original = MeshPacket(
            protocolVersion = 1,
            packetId = UUID.randomUUID().toString(),
            type = MeshPacketType.TEXT_MESSAGE,
            originDeviceId = "RNV-A1B2-C3D4-E5F6",
            destinationDeviceId = "RNV-9876-5432-10FE",
            senderPublicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A",
            payload = "EncryptedTextPayloadBase64==",
            timestamp = 1700000000000L,
            expiryTimestamp = 1700086400000L,
            hopLimit = 5,
            currentHop = 2,
            routePath = listOf("RNV-HOP1", "RNV-HOP2"),
            signature = "HMAC_SIG_XYZ"
        )

        val bytes = original.toBytes()
        val deserialized = MeshPacket.fromBytes(bytes)

        assertEquals(original.packetId, deserialized.packetId)
        assertEquals(original.type, deserialized.type)
        assertEquals(original.originDeviceId, deserialized.originDeviceId)
        assertEquals(original.destinationDeviceId, deserialized.destinationDeviceId)
        assertEquals(original.senderPublicKeyBase64, deserialized.senderPublicKeyBase64)
        assertEquals(original.payload, deserialized.payload)
        assertEquals(original.timestamp, deserialized.timestamp)
        assertEquals(original.expiryTimestamp, deserialized.expiryTimestamp)
        assertEquals(original.hopLimit, deserialized.hopLimit)
        assertEquals(original.currentHop, deserialized.currentHop)
        assertEquals(original.routePath, deserialized.routePath)
        assertEquals(original.signature, deserialized.signature)
    }

    @Test
    fun `hop limit enforcement stops propagation`() {
        val packet = MeshPacket(
            type = MeshPacketType.TEXT_MESSAGE,
            originDeviceId = "RNV-SRC",
            destinationDeviceId = "RNV-DST",
            hopLimit = 3,
            currentHop = 2
        )
        assertTrue(packet.canHopFurther())

        val hopped = packet.withIncrementedHop("RNV-RELAY-1")
        assertEquals(3, hopped.currentHop)
        assertFalse(hopped.canHopFurther())
    }

    @Test
    fun `packet expiration check`() {
        val expiredPacket = MeshPacket(
            type = MeshPacketType.TEXT_MESSAGE,
            originDeviceId = "RNV-SRC",
            destinationDeviceId = "RNV-DST",
            expiryTimestamp = System.currentTimeMillis() - 1000L
        )
        assertTrue(expiredPacket.isExpired())

        val validPacket = MeshPacket(
            type = MeshPacketType.TEXT_MESSAGE,
            originDeviceId = "RNV-SRC",
            destinationDeviceId = "RNV-DST",
            expiryTimestamp = System.currentTimeMillis() + 60000L
        )
        assertFalse(validPacket.isExpired())
    }

    @Test
    fun `duplicate detection and route path prevents loops`() {
        val packet = MeshPacket(
            type = MeshPacketType.TEXT_MESSAGE,
            originDeviceId = "RNV-SRC",
            destinationDeviceId = "RNV-DST",
            routePath = listOf("RNV-NODE-A", "RNV-NODE-B")
        )

        // Intermediate node C checks if it already forwarded or visited
        assertTrue(packet.routePath.contains("RNV-NODE-A"))
        assertTrue(packet.routePath.contains("RNV-NODE-B"))
        assertFalse(packet.routePath.contains("RNV-NODE-C"))

        val nextHop = packet.withIncrementedHop("RNV-NODE-C")
        assertTrue(nextHop.routePath.contains("RNV-NODE-C"))
        assertEquals(3, nextHop.routePath.size)
    }
}
