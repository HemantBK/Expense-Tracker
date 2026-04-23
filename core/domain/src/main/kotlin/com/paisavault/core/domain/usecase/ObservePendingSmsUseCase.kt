// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePendingSmsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<List<Transaction>> = repository.observePendingSms()
}

class ObservePendingSmsCount @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<Int> = repository.observePendingSmsCount()
}
