// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.repository

import com.paisavault.core.common.result.Result
import com.paisavault.core.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAll(): Flow<List<Budget>>
    fun observeForCategory(categoryId: Long): Flow<Budget?>
    suspend fun upsert(budget: Budget): Result<Long>
    suspend fun deleteForCategory(categoryId: Long): Result<Unit>
}
