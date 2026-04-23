// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.error.DomainError
import com.paisavault.core.common.result.Result
import com.paisavault.core.domain.repository.ExportSink
import com.paisavault.core.domain.security.Encryptor
import javax.inject.Inject

/**
 * Encrypts the user's verified-transaction CSV with their passphrase and writes the
 * resulting blob to [targetUri]. The passphrase array is wiped after use.
 */
class ExportEncryptedCsvUseCase @Inject constructor(
    private val buildCsv: ExportTrainingDataUseCase,
    private val encryptor: Encryptor,
    private val sink: ExportSink,
) {
    suspend operator fun invoke(targetUri: String, passphrase: CharArray): Result<Unit> =
        try {
            val csv = buildCsv()
            val blob = encryptor.encrypt(csv.toByteArray(Charsets.UTF_8), passphrase)
            sink.writeBytes(targetUri, blob)
            Result.Success(Unit)
        } catch (t: Throwable) {
            Result.Failure(DomainError.Unknown(t))
        } finally {
            passphrase.fill('\u0000')
        }
}
