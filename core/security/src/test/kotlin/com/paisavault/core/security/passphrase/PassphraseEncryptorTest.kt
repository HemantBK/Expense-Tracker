// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security.passphrase

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class PassphraseEncryptorTest {

    private val encryptor = PassphraseEncryptor()
    private val plaintext = "merchant,category\nZOMATO,Food\nUBER,Transport\n".toByteArray()

    @Test
    fun `roundtrip recovers plaintext`() {
        val pw = "correct horse battery staple".toCharArray()
        val blob = encryptor.encrypt(plaintext, pw.copyOf())
        val recovered = encryptor.decrypt(blob, pw)
        recovered.contentEquals(plaintext).shouldBeTrue()
    }

    @Test
    fun `wrong passphrase fails`() {
        val correctPw = "right passphrase".toCharArray()
        val wrongPw = "wrong passphrase".toCharArray()
        val blob = encryptor.encrypt(plaintext, correctPw.copyOf())
        shouldThrow<Exception> { encryptor.decrypt(blob, wrongPw) }
    }

    @Test
    fun `tampered ciphertext fails GCM auth`() {
        val pw = "pw".toCharArray()
        val blob = encryptor.encrypt(plaintext, pw.copyOf())
        // Flip a byte in the ciphertext body (past the 40-byte header).
        blob[blob.size - 1] = (blob[blob.size - 1].toInt() xor 1).toByte()
        shouldThrow<Exception> { encryptor.decrypt(blob, pw) }
    }

    @Test
    fun `bad magic is rejected`() {
        val pw = "pw".toCharArray()
        val blob = encryptor.encrypt(plaintext, pw.copyOf())
        blob[0] = 'X'.code.toByte()
        shouldThrow<Exception> { encryptor.decrypt(blob, pw) }
    }

    @Test
    fun `different encryptions of same text differ`() {
        val pw = "pw".toCharArray()
        val a = encryptor.encrypt(plaintext, pw.copyOf())
        val b = encryptor.encrypt(plaintext, pw.copyOf())
        // Salts + IVs are random, so blobs should differ.
        a.contentEquals(b) shouldBe false
    }

    @Test
    fun `empty plaintext roundtrips`() {
        val pw = "pw".toCharArray()
        val blob = encryptor.encrypt(ByteArray(0), pw.copyOf())
        val recovered = encryptor.decrypt(blob, pw)
        recovered.size shouldBe 0
        blob.size shouldNotBe 0
    }
}
