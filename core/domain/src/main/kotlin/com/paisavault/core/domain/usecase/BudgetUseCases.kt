// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.result.Result
import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.model.Budget
import com.paisavault.core.domain.model.BudgetPeriod
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.model.Money
import com.paisavault.core.domain.model.TransactionType
import com.paisavault.core.domain.repository.BudgetRepository
import com.paisavault.core.domain.repository.CategoryRepository
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.core.domain.util.monthRangeOf
import com.paisavault.core.domain.util.weekRangeOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

class ObserveBudgetsUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
) {
    operator fun invoke(): Flow<List<Budget>> = budgetRepository.observeAll()
}

class SetBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        categoryId: Long,
        amountMinor: Long,
        period: BudgetPeriod = BudgetPeriod.Monthly,
        currency: String = "INR",
    ): Result<Long> {
        require(amountMinor > 0) { "budget amount must be > 0" }
        val budget = Budget(
            id = 0,
            categoryId = categoryId,
            amount = Money(minorUnits = amountMinor, currency = currency),
            period = period,
            startDate = clock.now(),
        )
        return budgetRepository.upsert(budget)
    }
}

class DeleteBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
) {
    suspend operator fun invoke(categoryId: Long): Result<Unit> =
        budgetRepository.deleteForCategory(categoryId)
}

/** Full spend-vs-budget snapshot for all categories with budgets set. */
class ObserveBudgetStatusesUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<BudgetStatus>> =
        combine(budgetRepository.observeAll(), categoryRepository.observeAll()) { budgets, cats ->
            budgets to cats
        }.flatMapLatest { (budgets, categories) ->
            if (budgets.isEmpty()) return@flatMapLatest flowOf(emptyList())
            val categoriesById = categories.associateBy { it.id }
            val flows = budgets.map { budget ->
                val (start, end) = rangeFor(budget.period, clock.now())
                transactionRepository.observeInRange(start, end).map { txns ->
                    val spent = txns.asSequence()
                        .filter { it.type == TransactionType.Debit }
                        .filter { it.categoryId == budget.categoryId }
                        .sumOf { it.amount.minorUnits }
                    BudgetStatus(
                        budget = budget,
                        category = categoriesById[budget.categoryId],
                        spentMinor = spent,
                    )
                }
            }
            combine(flows) { statuses -> statuses.toList() }
        }

    private fun rangeFor(period: BudgetPeriod, now: Instant): Pair<Instant, Instant> = when (period) {
        BudgetPeriod.Monthly -> monthRangeOf(now).let { it.start to it.end }
        BudgetPeriod.Weekly -> weekRangeOf(now).let { it.start to it.end }
    }
}

data class BudgetStatus(
    val budget: Budget,
    val category: Category?,
    val spentMinor: Long,
) {
    val fraction: Float
        get() = if (budget.amount.minorUnits == 0L) {
            0f
        } else {
            (spentMinor.toFloat() / budget.amount.minorUnits.toFloat()).coerceAtLeast(0f)
        }

    val isExceeded: Boolean get() = spentMinor > budget.amount.minorUnits
    val isNearLimit: Boolean get() = !isExceeded && fraction >= NEAR_LIMIT_FRACTION

    private companion object {
        const val NEAR_LIMIT_FRACTION = 0.8f
    }
}
