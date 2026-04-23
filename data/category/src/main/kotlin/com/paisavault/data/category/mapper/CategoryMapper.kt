// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.category.mapper

import com.paisavault.core.database.entity.CategoryEntity
import com.paisavault.core.domain.model.Category

internal fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    colorArgb = colorArgb,
    isDefault = isDefault,
)

internal fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    colorArgb = colorArgb,
    isDefault = isDefault,
)
