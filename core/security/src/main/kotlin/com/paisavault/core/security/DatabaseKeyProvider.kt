// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security

import javax.inject.Inject
import javax.inject.Singleton

/** Surfaces the SQLCipher passphrase to the database module without exposing Keystore internals. */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    private val keystoreManager: KeystoreManager,
) {
    fun getPassphrase(): ByteArray = keystoreManager.getOrCreateDbKey()
}
