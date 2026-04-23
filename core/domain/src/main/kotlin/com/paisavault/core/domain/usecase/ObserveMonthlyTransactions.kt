// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.core.domain.util.monthRangeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import javax.inject.Inject

class ObserveMonthlyTransactions @Inject constructor(
    private val repository: TransactionRepository,
    private val clock: Clock,
) {
    operator fun invoke(anchor: Instant = clock.now()): Flow<List<Transaction>> {
        val range = monthRangeOf(anchor)
        return repository.observeInRange(range.start, range.end)
    }
}
