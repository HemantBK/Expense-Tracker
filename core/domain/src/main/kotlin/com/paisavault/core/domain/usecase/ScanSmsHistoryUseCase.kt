// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.domain.repository.SmsHistorySource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ScanSmsHistoryUseCase @Inject constructor(
    private val source: SmsHistorySource,
) {
    operator fun invoke(sinceEpochMillis: Long = 0L): Flow<SmsHistorySource.ScanProgress> =
        source.scan(sinceEpochMillis)
}
