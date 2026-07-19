package com.maangatech.gojuagent.core.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local PIN gate used as the fast, offline-friendly unlock (biometric is preferred when
 * available; the PIN is always required as a fallback, per the "offline authentication
 * cache" requirement — a teller with no signal must still be able to unlock the app they're
 * already signed into). PIN is never transmitted or compared in plain text: PBKDF2-HMAC-SHA256
 * with a random per-install salt, matching the app's local-only threat model (the hash never
 * needs to match a peer, so a fast KDF iteration count that stays snappy on budget devices is
 * fine here — this is not a password used for remote auth).
 */
@Singleton
class PinAuthManager @Inject constructor(private val securePrefs: SecurePrefs) {

    fun setPin(pin: String) {
        require(pin.length in 4..8 && pin.all { it.isDigit() }) { "PIN must be 4-8 digits" }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        securePrefs.pinSalt = salt.toHexString()
        securePrefs.pinHash = hash(pin, salt)
    }

    fun verifyPin(pin: String): Boolean {
        val saltHex = securePrefs.pinSalt ?: return false
        val expectedHash = securePrefs.pinHash ?: return false
        val salt = saltHex.hexToByteArray()
        return hash(pin, salt) == expectedHash
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hashBytes = factory.generateSecret(spec).encoded
        return hashBytes.toHexString()
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToByteArray(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private companion object {
        const val ITERATIONS = 20_000
        const val KEY_LENGTH_BITS = 256
    }
}
