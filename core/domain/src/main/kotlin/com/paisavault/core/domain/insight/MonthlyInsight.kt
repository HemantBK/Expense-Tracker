// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.insight

import com.paisavault.core.domain.model.Category

/**
 * Descriptive finding derived from spending history. Kept in the domain layer as a pure
 * data shape; UI maps each variant to a user-facing string.
 */
sealed interface MonthlyInsight {
    val severity: Severity

    enum class Severity { Info, Warning, Alert }

    /** Current-month spend on [category] is [ratio]x the previous-month spend. */
    data class CategorySpendChanged(
        val category: Category,
        val previousMinor: Long,
        val currentMinor: Long,
        val ratio: Float,
    ) : MonthlyInsight {
        override val severity: Severity = when {
            ratio >= ALERT_RATIO -> Severity.Alert
            ratio >= WARN_RATIO -> Severity.Warning
            else -> Severity.Info
        }

        private companion object {
            const val WARN_RATIO = 1.3f
            const val ALERT_RATIO = 1.6f
        }
    }

    /** A single transaction [amountMinor] exceeds [thresholdMinor] = mean + 2σ for [category]. */
    data class TransactionAnomaly(
        val category: Category,
        val amountMinor: Long,
        val thresholdMinor: Long,
    ) : MonthlyInsight {
        override val severity: Severity = Severity.Warning
    }

    /** Approaching a budget limit (80%+). */
    data class BudgetNearLimit(
        val category: Category,
        val spentMinor: Long,
        val limitMinor: Long,
    ) : MonthlyInsight {
        override val severity: Severity = Severity.Warning
    }

    /** Budget exceeded. */
    data class BudgetExceeded(
        val category: Category,
        val spentMinor: Long,
        val limitMinor: Long,
    ) : MonthlyInsight {
        override val severity: Severity = Severity.Alert
    }
}
