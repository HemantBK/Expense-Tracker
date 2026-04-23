// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.transaction.export

import android.content.Context
import android.net.Uri
import com.paisavault.core.common.dispatcher.AppDispatchers
import com.paisavault.core.domain.repository.ExportSink
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes export bytes to a SAF-provided [Uri] string. Caller obtains the Uri via
 * `ActivityResultContracts.CreateDocument`.
 */
@Singleton
internal class SafExportSink @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
) : ExportSink {
    override suspend fun writeBytes(targetUri: String, bytes: ByteArray) {
        withContext(dispatchers.io) {
            val uri = Uri.parse(targetUri)
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: error("Cannot open output stream for $uri")
        }
    }
}
