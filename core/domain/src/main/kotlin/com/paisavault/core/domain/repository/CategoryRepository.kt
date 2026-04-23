// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.repository

import com.paisavault.core.common.result.Result
import com.paisavault.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun add(category: Category): Result<Long>
    suspend fun rename(id: Long, name: String): Result<Unit>
    suspend fun delete(id: Long): Result<Unit>
}
