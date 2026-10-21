package com.example.core.crypto

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Production-ready cryptographic manager for Ronova Bluetooth Mesh Messenger.
 * Uses EC secp256r1 (prime256v1) for ECDH key exchange and AES-256-GCM for authenticated encryption.
 * Generates stable cryptographic Device IDs from public keys (SHA-256 fingerprint).
 */
class CryptoManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ronova_crypto_identity", Context.MODE_PRIVATE)

    private val secureRandom = SecureRandom()
    private var myKeyPair: KeyPair

    init {
        myKeyPair = loadOrGenerateKeyPair()
    }

    val publicKey: PublicKey
        get() = myKeyPair.public

    val privateKey: PrivateKey
        get() = myKeyPair.private

    val publicKeyBase64: String
        get() = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)

    /**
     * Unique stable cryptographic device identifier, derived as formatted SHA-256 fingerprint.
     * Example: RONOVA-7A3F-89B2-C4D1
     */
    val deviceId: String by lazy {
        computeFingerprint(publicKey.encoded)
    }

    private fun loadOrGenerateKeyPair(): KeyPair {
        val pubStr = prefs.getString(KEY_PUBLIC, null)
        val privStr = prefs.getString(KEY_PRIVATE, null)

        if (pubStr != null && privStr != null) {
            try {
                val keyFactory = KeyFactory.getInstance("EC")
                val pubBytes = Base64.decode(pubStr, Base64.DEFAULT)
                val privBytes = Base64.decode(privStr, Base64.DEFAULT)
                val pubKey = keyFactory.generatePublic(X509EncodedKeySpec(pubBytes))
                val privKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(privBytes))
                return KeyPair(pubKey, privKey)
            } catch (e: Exception) {
                // Key corrupted or invalid, regenerate
            }
        }

        // Generate new EC KeyPair
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"), secureRandom)
        val newPair = kpg.generateKeyPair()

        prefs.edit()
            .putString(KEY_PUBLIC, Base64.encodeToString(newPair.public.encoded, Base64.NO_WRAP))
            .putString(KEY_PRIVATE, Base64.encodeToString(newPair.private.encoded, Base64.NO_WRAP))
            .apply()

        return newPair
    }

    fun computeFingerprint(keyBytes: ByteArray): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash = sha256.digest(keyBytes)
        val hex = hash.joinToString("") { "%02X".format(it) }
        return "RNV-${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}"
    }

    /**
     * Parses a Base64-encoded X509 EC public key.
     */
    fun decodePublicKey(base64: String): PublicKey {
        val keyBytes = Base64.decode(base64, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
    }

    /**
     * Derives a symmetric AES-256 key using ECDH shared secret and SHA-256 KDF.
     */
    private fun deriveSharedKey(peerPublicKey: PublicKey): SecretKeySpec {
        val ka = KeyAgreement.getInstance("ECDH")
        ka.init(privateKey)
        ka.doPhase(peerPublicKey, true)
        val sharedSecret = ka.generateSecret()

        val md = MessageDigest.getInstance("SHA-256")
        val derivedKey = md.digest(sharedSecret)
        return SecretKeySpec(derivedKey, "AES")
    }

    /**
     * Encrypts plaintext using AES-256-GCM authenticated encryption for the recipient.
     * Returns Base64-encoded bundle: [12-byte IV] + [Ciphertext + 16-byte Auth Tag]
     */
    fun encryptForPeer(plaintext: String, recipientPublicKey: PublicKey): String {
        val key = deriveSharedKey(recipientPublicKey)
        val iv = ByteArray(12)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(ciphertext, 0, combined, iv.size, ciphertext.size)

        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts AES-256-GCM ciphertext from a peer using peer's public key.
     */
    fun decryptFromPeer(encryptedBase64: String, senderPublicKey: PublicKey): String {
        val combined = Base64.decode(encryptedBase64, Base64.DEFAULT)
        if (combined.size < 12 + 16) {
            throw IllegalArgumentException("Malformed encrypted payload")
        }

        val iv = ByteArray(12)
        System.arraycopy(combined, 0, iv, 0, 12)
        val ciphertext = ByteArray(combined.size - 12)
        System.arraycopy(combined, 12, ciphertext, 0, ciphertext.size)

        val key = deriveSharedKey(senderPublicKey)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val plaintextBytes = cipher.doFinal(ciphertext)
        return String(plaintextBytes, Charsets.UTF_8)
    }

    /**
     * Computes HMAC-SHA256 for packet integrity.
     */
    fun computeHmac(data: ByteArray): String {
        val mac = Mac.getInstance("HmacSHA256")
        val key = SecretKeySpec(privateKey.encoded.take(32).toByteArray(), "HmacSHA256")
        mac.init(key)
        val signature = mac.doFinal(data)
        return Base64.encodeToString(signature, Base64.NO_WRAP)
    }

    /**
     * PRD v2.1 Section 6 Profile Lock & Authorization Credential Management.
     */
    fun isProfileLocked(): Boolean {
        return prefs.getBoolean(KEY_PROFILE_LOCKED, false)
    }

    fun setProfileLocked(locked: Boolean) {
        prefs.edit().putBoolean(KEY_PROFILE_LOCKED, locked).apply()
    }

    fun getOrGenerateAuthorizationCredential(): String {
        val existing = prefs.getString(KEY_AUTH_CREDENTIAL, null)
        if (existing != null) return existing
        return rotateAuthorizationCredential()
    }

    fun rotateAuthorizationCredential(): String {
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val sha256 = MessageDigest.getInstance("SHA-256")
        sha256.update(privateKey.encoded)
        val digest = sha256.digest(salt)
        val hex = digest.joinToString("") { "%02X".format(it) }
        val newCode = "AUTH-${hex.substring(0, 4)}-${hex.substring(4, 8)}-${hex.substring(8, 12)}"
        prefs.edit()
            .putString(KEY_AUTH_CREDENTIAL, newCode)
            .putLong(KEY_AUTH_CREDENTIAL_ROTATED_AT, System.currentTimeMillis())
            .apply()
        return newCode
    }

    fun getCredentialRotatedTimestamp(): Long {
        return prefs.getLong(KEY_AUTH_CREDENTIAL_ROTATED_AT, System.currentTimeMillis())
    }

    fun verifyAuthorizationCredential(candidate: String, expected: String): Boolean {
        val cleanedCandidate = candidate.trim().uppercase()
        val cleanedExpected = expected.trim().uppercase()
        return MessageDigest.isEqual(cleanedCandidate.toByteArray(), cleanedExpected.toByteArray())
    }

    fun resetIdentity(): KeyPair {
        prefs.edit().clear().apply()
        myKeyPair = loadOrGenerateKeyPair()
        return myKeyPair
    }

    companion object {
        private const val KEY_PUBLIC = "ec_public_key"
        private const val KEY_PRIVATE = "ec_private_key"
        private const val KEY_PROFILE_LOCKED = "key_profile_locked"
        private const val KEY_AUTH_CREDENTIAL = "key_auth_credential"
        private const val KEY_AUTH_CREDENTIAL_ROTATED_AT = "key_auth_credential_rotated_at"
    }
}
