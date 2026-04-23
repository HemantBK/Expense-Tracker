// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.category

import com.paisavault.core.database.entity.CategoryEntity

/** Seed data inserted on first launch. Icon keys map to drawables in :core:design-system. */
internal val DefaultCategories: List<CategoryEntity> = listOf(
    CategoryEntity(name = "Food", icon = "restaurant", colorArgb = 0xFFFF8A65.toInt(), isDefault = true),
    CategoryEntity(name = "Transport", icon = "directions_car", colorArgb = 0xFF4FC3F7.toInt(), isDefault = true),
    CategoryEntity(name = "Shopping", icon = "shopping_bag", colorArgb = 0xFFBA68C8.toInt(), isDefault = true),
    CategoryEntity(name = "Bills", icon = "receipt_long", colorArgb = 0xFF81C784.toInt(), isDefault = true),
    CategoryEntity(name = "Entertainment", icon = "movie", colorArgb = 0xFFFFB74D.toInt(), isDefault = true),
    CategoryEntity(name = "Health", icon = "local_hospital", colorArgb = 0xFFE57373.toInt(), isDefault = true),
    CategoryEntity(name = "Groceries", icon = "local_grocery_store", colorArgb = 0xFFAED581.toInt(), isDefault = true),
    CategoryEntity(name = "Other", icon = "category", colorArgb = 0xFF90A4AE.toInt(), isDefault = true),
)
