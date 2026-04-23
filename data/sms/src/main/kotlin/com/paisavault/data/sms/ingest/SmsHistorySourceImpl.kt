// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.ingest

import com.paisavault.core.domain.repository.SmsHistorySource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class SmsHistorySourceImpl @Inject constructor(
    private val scanner: SmsHistoricalScanner,
) : SmsHistorySource {
    override fun scan(sinceEpochMillis: Long): Flow<SmsHistorySource.ScanProgress> =
        scanner.scan(sinceEpochMillis).map {
            SmsHistorySource.ScanProgress(processed = it.processed, total = it.total)
        }
}
