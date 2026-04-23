// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.repository.CategoryRepository
import com.paisavault.core.domain.repository.CategorySpend
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.core.domain.util.monthRangeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class MonthlyDashboard(
    val totalDebitMinor: Long,
    val recentTransactions: List<Transaction>,
    val categoryBreakdown: List<CategorySpendWithCategory>,
)

data class CategorySpendWithCategory(
    val category: Category?,
    val spend: CategorySpend,
)

class ObserveMonthlyDashboard @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<MonthlyDashboard> {
        val range = monthRangeOf(clock.now())
        return combine(
            transactionRepository.observeTotalDebitMinor(range.start, range.end),
            transactionRepository.observeInRange(range.start, range.end),
            transactionRepository.observeCategoryBreakdown(range.start, range.end),
            categoryRepository.observeAll(),
        ) { total, txns, breakdown, categories ->
            val categoriesById = categories.associateBy { it.id }
            MonthlyDashboard(
                totalDebitMinor = total,
                recentTransactions = txns.take(RECENT_COUNT),
                categoryBreakdown = breakdown.map {
                    CategorySpendWithCategory(
                        category = categoriesById[it.categoryId],
                        spend = it,
                    )
                },
            )
        }
    }

    private companion object {
        const val RECENT_COUNT = 5
    }
}
