// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.repository

import com.paisavault.core.common.result.Result
import com.paisavault.core.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface TransactionRepository {
    fun observeInRange(start: Instant, end: Instant): Flow<List<Transaction>>
    fun observeTotalDebitMinor(start: Instant, end: Instant): Flow<Long>
    fun observeCategoryBreakdown(start: Instant, end: Instant): Flow<List<CategorySpend>>
    fun observePendingSms(): Flow<List<Transaction>>
    fun observePendingSmsCount(): Flow<Int>
    suspend fun add(txn: Transaction): Result<Long>
    suspend fun update(txn: Transaction): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
    suspend fun markVerified(id: Long): Result<Unit>
}

data class CategorySpend(val categoryId: Long, val totalMinor: Long)
