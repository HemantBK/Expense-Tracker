// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps AndroidX Security's [EncryptedSharedPreferences] to persist small secrets
 * (DB passphrase, feature flags, user prefs) encrypted with a hardware-backed master key.
 *
 * See BUILD.md § 23.
 */
@Singleton
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setUserAuthenticationRequired(false)
            .build()
    }

    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** Returns an existing or freshly generated 256-bit DB passphrase. */
    fun getOrCreateDbKey(): ByteArray {
        val encoded = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (encoded != null) {
            return Base64.decode(encoded, Base64.NO_WRAP)
        }
        val fresh = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(fresh, Base64.NO_WRAP))
            .apply()
        return fresh
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    private companion object {
        const val PREFS_NAME = "paisa_secure_prefs"
        const val KEY_DB_PASSPHRASE = "db_passphrase"
        const val KEY_SIZE_BYTES = 32
    }
}
