// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.insight.MonthlyInsight
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.model.TransactionType
import com.paisavault.core.domain.repository.CategoryRepository
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.core.domain.util.monthRangeOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlin.math.sqrt
import javax.inject.Inject

/**
 * Emits a list of [MonthlyInsight]s derived from current-month spending versus the prior
 * month and a 90-day history for anomaly detection. Pure domain logic; no DB/time coupling
 * beyond the injected abstractions. See BUILD.md § 13.1.
 */
class ObserveMonthlyInsightsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val observeBudgetStatuses: ObserveBudgetStatusesUseCase,
    private val clock: Clock,
) {

    operator fun invoke(): Flow<List<MonthlyInsight>> {
        val now = clock.now()
        val current = monthRangeOf(now)
        val previousAnchor = now.minus(DateTimePeriod(months = 1), TimeZone.currentSystemDefault())
        val previous = monthRangeOf(previousAnchor)
        val historyStart = now.minus(DateTimePeriod(days = HISTORY_DAYS), TimeZone.currentSystemDefault())

        return combine(
            transactionRepository.observeInRange(current.start, current.end),
            transactionRepository.observeInRange(previous.start, previous.end),
            transactionRepository.observeInRange(historyStart, now),
            categoryRepository.observeAll(),
            observeBudgetStatuses(),
        ) { currentTxns, previousTxns, historyTxns, categories, budgetStatuses ->
            buildList {
                addAll(compareMonths(currentTxns, previousTxns, categories))
                addAll(anomalies(currentTxns, historyTxns, categories, now))
                addAll(budgetInsights(budgetStatuses))
            }.sortedByDescending { it.severity.ordinal }
        }
    }

    private fun compareMonths(
        current: List<Transaction>,
        previous: List<Transaction>,
        categories: List<Category>,
    ): List<MonthlyInsight> {
        val currentByCat = current.debits().groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount.minorUnits } }
        val previousByCat = previous.debits().groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount.minorUnits } }
        val categoriesById = categories.associateBy { it.id }

        return currentByCat.mapNotNull { (catId, currentTotal) ->
            val previousTotal = previousByCat[catId] ?: return@mapNotNull null
            if (previousTotal < MIN_COMPARE_MINOR) return@mapNotNull null
            val ratio = currentTotal.toFloat() / previousTotal.toFloat()
            if (ratio < REPORT_RATIO_FLOOR) return@mapNotNull null
            val category = categoriesById[catId] ?: return@mapNotNull null
            MonthlyInsight.CategorySpendChanged(
                category = category,
                previousMinor = previousTotal,
                currentMinor = currentTotal,
                ratio = ratio,
            )
        }
    }

    private fun anomalies(
        currentTxns: List<Transaction>,
        historyTxns: List<Transaction>,
        categories: List<Category>,
        now: Instant,
    ): List<MonthlyInsight> {
        val categoriesById = categories.associateBy { it.id }
        val cutoff = now.minus(DateTimePeriod(days = ANOMALY_WINDOW_DAYS), TimeZone.currentSystemDefault())

        val byCategory = historyTxns.debits().groupBy { it.categoryId }
        return currentTxns.debits()
            .filter { it.date >= cutoff }
            .mapNotNull { txn ->
                val sample = byCategory[txn.categoryId] ?: return@mapNotNull null
                if (sample.size < MIN_SAMPLE_SIZE) return@mapNotNull null
                val values = sample.map { it.amount.minorUnits }
                val mean = values.average()
                val variance = values.map { (it - mean) * (it - mean) }.average()
                val std = sqrt(variance)
                val threshold = mean + ANOMALY_SIGMAS * std
                if (txn.amount.minorUnits.toDouble() <= threshold) return@mapNotNull null
                val category = categoriesById[txn.categoryId] ?: return@mapNotNull null
                MonthlyInsight.TransactionAnomaly(
                    category = category,
                    amountMinor = txn.amount.minorUnits,
                    thresholdMinor = threshold.toLong(),
                )
            }
            .distinctBy { it.category.id to it.amountMinor }
    }

    private fun budgetInsights(statuses: List<BudgetStatus>): List<MonthlyInsight> =
        statuses.mapNotNull { status ->
            val category = status.category ?: return@mapNotNull null
            when {
                status.isExceeded -> MonthlyInsight.BudgetExceeded(
                    category = category,
                    spentMinor = status.spentMinor,
                    limitMinor = status.budget.amount.minorUnits,
                )
                status.isNearLimit -> MonthlyInsight.BudgetNearLimit(
                    category = category,
                    spentMinor = status.spentMinor,
                    limitMinor = status.budget.amount.minorUnits,
                )
                else -> null
            }
        }

    private fun List<Transaction>.debits() = filter { it.type == TransactionType.Debit }

    @Suppress("unused")
    private fun Instant.inFuture(now: Instant): Instant = now.plus(DateTimePeriod(days = 0), TimeZone.UTC)

    private companion object {
        const val HISTORY_DAYS = 90
        const val ANOMALY_WINDOW_DAYS = 30
        const val ANOMALY_SIGMAS = 2.0
        const val MIN_SAMPLE_SIZE = 5
        const val MIN_COMPARE_MINOR = 10_000L // Rs 100 — below this, ratios are noisy
        const val REPORT_RATIO_FLOOR = 1.3f
    }
}
