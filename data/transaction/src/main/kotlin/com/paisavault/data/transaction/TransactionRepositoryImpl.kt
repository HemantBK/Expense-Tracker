// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.transaction

import com.paisavault.core.common.error.DomainError
import com.paisavault.core.common.result.Result
import com.paisavault.core.common.time.Clock
import com.paisavault.core.database.dao.TransactionDao
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.repository.CategorySpend
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.data.transaction.mapper.toDomain
import com.paisavault.data.transaction.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao,
    private val clock: Clock,
) : TransactionRepository {

    override fun observeInRange(start: Instant, end: Instant): Flow<List<Transaction>> =
        dao.observeInRange(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .map { list -> list.map { it.toDomain() } }

    override fun observeTotalDebitMinor(start: Instant, end: Instant): Flow<Long> =
        dao.observeTotalDebit(start.toEpochMilliseconds(), end.toEpochMilliseconds())

    override fun observeCategoryBreakdown(start: Instant, end: Instant): Flow<List<CategorySpend>> =
        dao.observeCategoryBreakdown(start.toEpochMilliseconds(), end.toEpochMilliseconds())
            .map { rows -> rows.map { CategorySpend(categoryId = it.categoryId ?: 0L, totalMinor = it.totalMinor) } }

    override suspend fun add(txn: Transaction): Result<Long> = safeCall {
        dao.insert(txn.toEntity(createdAtMillis = clock.now().toEpochMilliseconds()))
    }

    override suspend fun update(txn: Transaction): Result<Unit> = safeCall {
        dao.update(txn.toEntity(createdAtMillis = clock.now().toEpochMilliseconds()))
    }

    override suspend fun delete(id: Long): Result<Unit> = safeCall { dao.deleteById(id) }

    override fun observePendingSms(): Flow<List<Transaction>> =
        dao.observePendingSms().map { list -> list.map { it.toDomain() } }

    override fun observePendingSmsCount(): Flow<Int> = dao.observePendingSmsCount()

    override suspend fun markVerified(id: Long): Result<Unit> = safeCall { dao.markVerified(id) }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (t: Throwable) {
        Result.Failure(DomainError.Unknown(t))
    }
}
