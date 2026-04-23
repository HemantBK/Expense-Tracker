// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.paisavault.feature.add.AddTxnScreen
import com.paisavault.feature.home.HomeScreen
import com.paisavault.feature.settings.SettingsScreen
import com.paisavault.feature.settings.budget.BudgetsScreen
import com.paisavault.feature.settings.export.ExportScreen
import com.paisavault.feature.smsreview.SmsReviewScreen
import com.paisavault.feature.stats.StatsScreen
import com.paisavault.feature.transactions.TransactionsScreen
import kotlin.reflect.KClass

private data class TopLevelTab(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    val label: String,
)

private val topLevelTabs = listOf(
    TopLevelTab(HomeRoute, HomeRoute::class, Icons.Filled.Home, "Home"),
    TopLevelTab(TransactionsRoute, TransactionsRoute::class, Icons.AutoMirrored.Filled.List, "Transactions"),
    TopLevelTab(StatsRoute, StatsRoute::class, Icons.Filled.PieChart, "Stats"),
    TopLevelTab(SettingsRoute, SettingsRoute::class, Icons.Filled.Settings, "Settings"),
)

@Composable
fun PaisaNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = topLevelTabs.any { tab ->
        currentDestination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelTabs.forEach { tab ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(HomeRoute) { saveState = true }
                                }
                            },
                            icon = { Icon(imageVector = tab.icon, contentDescription = null) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onAddClick = { navController.navigate(AddTxnRoute()) },
                    onTransactionClick = { navController.navigate(AddTxnRoute(editId = it)) },
                    onSeeAllClick = { navController.navigate(TransactionsRoute) },
                    onReviewSmsClick = { navController.navigate(SmsReviewRoute) },
                )
            }
            composable<TransactionsRoute> {
                TransactionsScreen(
                    onEditTransaction = { navController.navigate(AddTxnRoute(editId = it)) },
                )
            }
            composable<AddTxnRoute> {
                AddTxnScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable<StatsRoute> { StatsScreen() }
            composable<SettingsRoute> {
                SettingsScreen(
                    onNavigateToBudgets = { navController.navigate(BudgetsRoute) },
                    onNavigateToExport = { navController.navigate(ExportRoute) },
                )
            }
            composable<BudgetsRoute> {
                BudgetsScreen(onBack = { navController.popBackStack() })
            }
            composable<ExportRoute> {
                ExportScreen(onBack = { navController.popBackStack() })
            }
            composable<SmsReviewRoute> { SmsReviewScreen() }
        }
    }
}
