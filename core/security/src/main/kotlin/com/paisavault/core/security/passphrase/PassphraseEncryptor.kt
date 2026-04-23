// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security.passphrase

import com.paisavault.core.domain.security.Encryptor
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Symmetric encryption for user-passphrase-protected exports. PBKDF2-HMAC-SHA256 KDF
 * (600k iterations) + AES-256-GCM. See BUILD.md § 23.1.
 *
 * Wire format:
 *
 * ```
 * | 4 bytes magic  "PV01"
 * | 4 bytes version (int, big-endian)
 * | 4 bytes kdf iter count (int, big-endian)
 * | 16 bytes salt
 * | 12 bytes GCM IV
 * |   .. ciphertext + 16 byte GCM tag (appended by Cipher)
 * ```
 */
@Singleton
class PassphraseEncryptor @Inject constructor() : Encryptor {

    /** Encrypt [plaintext] under [passphrase]; returns the wire-format blob. */
    override fun encrypt(plaintext: ByteArray, passphrase: CharArray): ByteArray {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(GCM_IV_BYTES).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(passphrase, salt, KDF_ITERATIONS)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        val ciphertext = cipher.doFinal(plaintext)

        val buf = ByteBuffer.allocate(HEADER_BYTES + ciphertext.size)
        buf.put(MAGIC)
        buf.putInt(VERSION)
        buf.putInt(KDF_ITERATIONS)
        buf.put(salt)
        buf.put(iv)
        buf.put(ciphertext)
        return buf.array()
    }

    /** Decrypt a blob produced by [encrypt]. Throws on wrong passphrase or tamper. */
    override fun decrypt(blob: ByteArray, passphrase: CharArray): ByteArray {
        require(blob.size >= HEADER_BYTES) { "ciphertext too short" }
        val buf = ByteBuffer.wrap(blob)
        val magic = ByteArray(MAGIC.size).also { buf.get(it) }
        require(magic.contentEquals(MAGIC)) { "bad magic header" }
        val version = buf.int
        require(version == VERSION) { "unsupported version: $version" }
        val iterations = buf.int
        require(iterations in MIN_ITERATIONS..MAX_ITERATIONS) { "invalid kdf iterations" }
        val salt = ByteArray(SALT_BYTES).also { buf.get(it) }
        val iv = ByteArray(GCM_IV_BYTES).also { buf.get(it) }
        val ciphertext = ByteArray(blob.size - HEADER_BYTES).also { buf.get(it) }

        val key = deriveKey(passphrase, salt, iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv))
        }
        return cipher.doFinal(ciphertext)
    }

    private fun deriveKey(passphrase: CharArray, salt: ByteArray, iterations: Int): SecretKey {
        val spec = PBEKeySpec(passphrase, salt, iterations, KEY_BITS)
        val raw = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(raw, "AES")
    }

    companion object {
        private val MAGIC = byteArrayOf('P'.code.toByte(), 'V'.code.toByte(), '0'.code.toByte(), '1'.code.toByte())
        private const val VERSION = 1
        private const val KDF_ITERATIONS = 600_000
        private const val MIN_ITERATIONS = 100_000
        private const val MAX_ITERATIONS = 2_000_000
        private const val SALT_BYTES = 16
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BITS = 128
        private const val KEY_BITS = 256
        private const val HEADER_BYTES = 4 + 4 + 4 + SALT_BYTES + GCM_IV_BYTES
    }
}
