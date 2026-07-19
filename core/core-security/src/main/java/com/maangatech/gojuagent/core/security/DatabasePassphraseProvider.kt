package com.maangatech.gojuagent.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates (once) and retrieves the random passphrase used to open the SQLCipher-encrypted
 * Room database. The passphrase itself lives in EncryptedSharedPreferences — i.e. it is
 * wrapped by a Keystore-backed key, never hardcoded, never derived from anything guessable
 * (not the PIN, not the device ID), so losing the app's private storage (e.g. via a
 * non-rooted backup) does not expose the DB key.
 */
@Singleton
class DatabasePassphraseProvider @Inject constructor(@ApplicationContext context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "goju_agent_db_key_store",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun getOrCreatePassphrase(): CharArray {
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) return existing.toCharArray()

        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val generated = bytes.joinToString("") { "%02x".format(it) }
        prefs.edit().putString(KEY_DB_PASSPHRASE, generated).apply()
        return generated.toCharArray()
    }

    private companion object {
        const val KEY_DB_PASSPHRASE = "db_passphrase_v1"
    }
}
