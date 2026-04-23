// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SmsHistorySource {
    fun scan(sinceEpochMillis: Long = 0L): Flow<ScanProgress>

    data class ScanProgress(val processed: Int, val total: Int) {
        val fraction: Float = if (total == 0) 1f else processed.toFloat() / total.toFloat()
    }
}
