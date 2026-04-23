// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.paisavault.core.designsystem.component.CategoryColorDot
import com.paisavault.core.designsystem.component.EmptyState
import com.paisavault.core.designsystem.component.LoadingState
import com.paisavault.core.designsystem.component.MoneyText
import com.paisavault.core.designsystem.theme.PaisaTokens
import com.paisavault.core.domain.insight.MonthlyInsight
import com.paisavault.core.domain.usecase.BudgetStatus
import com.paisavault.core.domain.usecase.CategorySpendWithCategory

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.stats_title)) }) },
    ) { padding ->
        when {
            state.isLoading -> LoadingState(Modifier.padding(padding))
            state.breakdown.isEmpty() && state.budgets.isEmpty() -> EmptyState(
                title = stringResource(R.string.stats_empty_title),
                subtitle = stringResource(R.string.stats_empty_subtitle),
                modifier = Modifier.padding(padding),
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(PaisaTokens.Spacing.m),
                verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.l),
            ) {
                TotalCard(state)
                if (state.insights.isNotEmpty()) InsightsSection(state.insights, state.currency)
                if (state.budgets.isNotEmpty()) BudgetsSection(state.budgets, state.currency)
                if (state.breakdown.isNotEmpty()) BreakdownSection(state.breakdown, state.totalMinor, state.currency)
            }
        }
    }
}

@Composable
private fun TotalCard(state: StatsState) {
    Column {
        Text(
            text = stringResource(R.string.stats_month_total),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MoneyText(
            amountMinor = state.totalMinor,
            currencyCode = state.currency,
            style = MaterialTheme.typography.displaySmall,
        )
    }
}

@Composable
private fun InsightsSection(insights: List<MonthlyInsight>, currency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s)) {
        Text(
            text = stringResource(R.string.stats_insights_title),
            style = MaterialTheme.typography.titleMedium,
        )
        insights.forEach { insight -> InsightCard(insight, currency) }
    }
}

@Composable
private fun InsightCard(insight: MonthlyInsight, currency: String) {
    val containerColor = when (insight.severity) {
        MonthlyInsight.Severity.Alert -> MaterialTheme.colorScheme.errorContainer
        MonthlyInsight.Severity.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        MonthlyInsight.Severity.Info -> MaterialTheme.colorScheme.secondaryContainer
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(PaisaTokens.Spacing.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = describe(insight, currency),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun describe(insight: MonthlyInsight, currency: String): String = when (insight) {
    is MonthlyInsight.CategorySpendChanged -> {
        val pct = ((insight.ratio - 1f) * PERCENT).toInt()
        stringResource(
            if (pct >= 0) R.string.stats_insight_spend_up else R.string.stats_insight_spend_down,
            insight.category.name, pct,
        )
    }
    is MonthlyInsight.TransactionAnomaly -> stringResource(
        R.string.stats_insight_anomaly,
        insight.category.name,
    )
    is MonthlyInsight.BudgetNearLimit -> stringResource(
        R.string.stats_insight_budget_near,
        insight.category.name,
    )
    is MonthlyInsight.BudgetExceeded -> stringResource(
        R.string.stats_insight_budget_over,
        insight.category.name,
    )
    else -> ""
}

@Composable
private fun BudgetsSection(budgets: List<BudgetStatus>, currency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s)) {
        Text(
            text = stringResource(R.string.stats_budgets_title),
            style = MaterialTheme.typography.titleMedium,
        )
        budgets.forEach { status -> BudgetRow(status, currency) }
    }
}

@Composable
private fun BudgetRow(status: BudgetStatus, currency: String) {
    val color = when {
        status.isExceeded -> MaterialTheme.colorScheme.error
        status.isNearLimit -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryColorDot(status.category?.colorArgb ?: 0xFF90A4AE.toInt())
                Text(
                    text = status.category?.name ?: stringResource(R.string.uncategorized),
                    modifier = Modifier.padding(start = PaisaTokens.Spacing.s),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                MoneyText(status.spentMinor, currency, style = MaterialTheme.typography.bodyMedium)
                Text(
                    " / ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                MoneyText(
                    status.budget.amount.minorUnits,
                    currency,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(3.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = status.fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(color = color, shape = RoundedCornerShape(3.dp)),
            )
        }
    }
}

@Composable
private fun BreakdownSection(items: List<CategorySpendWithCategory>, totalMinor: Long, currency: String) {
    Column(verticalArrangement = Arrangement.spacedBy(PaisaTokens.Spacing.s)) {
        Text(
            text = stringResource(R.string.stats_breakdown),
            style = MaterialTheme.typography.titleMedium,
        )
        items.forEach { item -> CategoryBar(item, totalMinor, currency) }
    }
}

@Composable
private fun CategoryBar(
    item: CategorySpendWithCategory,
    totalMinor: Long,
    currency: String,
) {
    val fraction = if (totalMinor > 0) {
        (item.spend.totalMinor.toFloat() / totalMinor.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val categoryColor = Color(item.category?.colorArgb ?: 0xFF90A4AE.toInt())

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryColorDot(item.category?.colorArgb ?: 0xFF90A4AE.toInt())
                Text(
                    text = item.category?.name ?: stringResource(R.string.uncategorized),
                    modifier = Modifier.padding(start = PaisaTokens.Spacing.s),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            MoneyText(
                amountMinor = item.spend.totalMinor,
                currencyCode = currency,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .padding(top = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(3.dp),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = fraction)
                    .height(6.dp)
                    .background(color = categoryColor, shape = RoundedCornerShape(3.dp)),
            )
        }
    }
}

private const val PERCENT = 100f
