// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.navigation

import kotlinx.serialization.Serializable

/** Type-safe nav destinations. See BUILD.md § 16. */
@Serializable
data object HomeRoute

@Serializable
data object TransactionsRoute

@Serializable
data class AddTxnRoute(val editId: Long? = null)

@Serializable
data object StatsRoute

@Serializable
data object SmsReviewRoute

@Serializable
data object SettingsRoute

@Serializable
data object OnboardingRoute

@Serializable
data object BudgetsRoute

@Serializable
data object ExportRoute
