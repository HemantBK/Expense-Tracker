// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.repository

/**
 * Platform port for writing an encrypted export blob to a user-chosen location.
 * Implementation on Android uses the Storage Access Framework (SAF) via ContentResolver.
 */
fun interface ExportSink {
    suspend fun writeBytes(targetUri: String, bytes: ByteArray)
}
