package com.example.core.protocol

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class MeshPacketType {
    BEACON,
    BEACON_RESP,
    TEXT_MESSAGE,
    ACK_DELIVERED,
    ROUTING_UPDATE,
    CALL_REQUEST,
    CALL_ACCEPT,
    CALL_REJECT,
    CALL_HANGUP,
    AUDIO_PACKET
}

data class MeshPacket(
    val protocolVersion: Int = 1,
    val packetId: String = UUID.randomUUID().toString(),
    val type: MeshPacketType,
    val originDeviceId: String,
    val destinationDeviceId: String,
    val senderPublicKeyBase64: String = "",
    val payload: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expiryTimestamp: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L), // 24hr TTL
    val hopLimit: Int = 5,
    val currentHop: Int = 0,
    val routePath: List<String> = emptyList(),
    val signature: String = ""
) {
    fun isExpired(): Boolean = System.currentTimeMillis() > expiryTimestamp

    fun canHopFurther(): Boolean = currentHop < hopLimit

    fun withIncrementedHop(relayNodeId: String): MeshPacket {
        val updatedPath = ArrayList(routePath).apply { add(relayNodeId) }
        return copy(
            currentHop = currentHop + 1,
            routePath = updatedPath
        )
    }

    fun toJsonObject(): JSONObject {
        val obj = JSONObject()
        obj.put("v", protocolVersion)
        obj.put("id", packetId)
        obj.put("type", type.name)
        obj.put("src", originDeviceId)
        obj.put("dst", destinationDeviceId)
        obj.put("pub", senderPublicKeyBase64)
        obj.put("pay", payload)
        obj.put("ts", timestamp)
        obj.put("exp", expiryTimestamp)
        obj.put("hl", hopLimit)
        obj.put("ch", currentHop)
        val arr = JSONArray()
        routePath.forEach { arr.put(it) }
        obj.put("rp", arr)
        obj.put("sig", signature)
        return obj
    }

    fun toBytes(): ByteArray {
        return toJsonObject().toString().toByteArray(Charsets.UTF_8)
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): MeshPacket {
            val typeStr = obj.optString("type", MeshPacketType.TEXT_MESSAGE.name)
            val type = try {
                MeshPacketType.valueOf(typeStr)
            } catch (e: Exception) {
                MeshPacketType.TEXT_MESSAGE
            }

            val routeArr = obj.optJSONArray("rp")
            val path = mutableListOf<String>()
            if (routeArr != null) {
                for (i in 0 until routeArr.length()) {
                    path.add(routeArr.getString(i))
                }
            }

            return MeshPacket(
                protocolVersion = obj.optInt("v", 1),
                packetId = obj.optString("id", UUID.randomUUID().toString()),
                type = type,
                originDeviceId = obj.optString("src", ""),
                destinationDeviceId = obj.optString("dst", ""),
                senderPublicKeyBase64 = obj.optString("pub", ""),
                payload = obj.optString("pay", ""),
                timestamp = obj.optLong("ts", System.currentTimeMillis()),
                expiryTimestamp = obj.optLong("exp", System.currentTimeMillis() + 86400000L),
                hopLimit = obj.optInt("hl", 5),
                currentHop = obj.optInt("ch", 0),
                routePath = path,
                signature = obj.optString("sig", "")
            )
        }

        fun fromBytes(bytes: ByteArray): MeshPacket {
            val str = String(bytes, Charsets.UTF_8)
            val obj = JSONObject(str)
            return fromJsonObject(obj)
        }
    }
}
