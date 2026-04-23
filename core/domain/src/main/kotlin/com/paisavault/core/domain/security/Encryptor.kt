// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.security

/**
 * Domain port for symmetric passphrase-based encryption. Implementation lives in
 * `:core:security` so `:core:domain` stays a pure-Kotlin / no-Android module.
 */
interface Encryptor {
    fun encrypt(plaintext: ByteArray, passphrase: CharArray): ByteArray
    fun decrypt(blob: ByteArray, passphrase: CharArray): ByteArray
}
