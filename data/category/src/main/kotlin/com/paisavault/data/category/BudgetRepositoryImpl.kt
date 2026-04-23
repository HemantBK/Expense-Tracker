// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.category

import com.paisavault.core.common.error.DomainError
import com.paisavault.core.common.result.Result
import com.paisavault.core.database.dao.BudgetDao
import com.paisavault.core.database.entity.BudgetEntity
import com.paisavault.core.domain.model.Budget
import com.paisavault.core.domain.model.BudgetPeriod
import com.paisavault.core.domain.model.Money
import com.paisavault.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao,
) : BudgetRepository {

    override fun observeAll(): Flow<List<Budget>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeForCategory(categoryId: Long): Flow<Budget?> =
        dao.observeForCategory(categoryId).map { it?.toDomain() }

    override suspend fun upsert(budget: Budget): Result<Long> = safeCall {
        dao.upsert(budget.toEntity())
    }

    override suspend fun deleteForCategory(categoryId: Long): Result<Unit> = safeCall {
        dao.deleteForCategory(categoryId)
    }

    private fun BudgetEntity.toDomain(): Budget = Budget(
        id = id,
        categoryId = categoryId,
        amount = Money(minorUnits = amountMinor, currency = currency),
        period = when (period.uppercase()) {
            "WEEKLY" -> BudgetPeriod.Weekly
            else -> BudgetPeriod.Monthly
        },
        startDate = Instant.fromEpochMilliseconds(startDateEpochMillis),
    )

    private fun Budget.toEntity(): BudgetEntity = BudgetEntity(
        id = id,
        categoryId = categoryId,
        amountMinor = amount.minorUnits,
        currency = amount.currency,
        period = period.name.uppercase(),
        startDateEpochMillis = startDate.toEpochMilliseconds(),
    )

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (t: Throwable) {
        Result.Failure(DomainError.Unknown(t))
    }
}
