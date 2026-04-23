// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.category

import com.paisavault.core.common.error.DomainError
import com.paisavault.core.common.result.Result
import com.paisavault.core.database.dao.CategoryDao
import com.paisavault.core.domain.model.Category
import com.paisavault.core.domain.repository.CategoryRepository
import com.paisavault.data.category.mapper.toDomain
import com.paisavault.data.category.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        dao.observeAll()
            .onStart { ensureSeeded() }
            .map { list -> list.map { it.toDomain() } }

    override suspend fun add(category: Category): Result<Long> = safeCall {
        dao.insert(category.toEntity())
    }

    override suspend fun rename(id: Long, name: String): Result<Unit> = safeCall {
        val existing = dao.getById(id) ?: return@safeCall
        dao.update(existing.copy(name = name))
    }

    override suspend fun delete(id: Long): Result<Unit> = safeCall {
        dao.deleteById(id)
    }

    private suspend fun ensureSeeded() {
        if (dao.count() == 0) {
            dao.insertAll(DefaultCategories)
        }
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.Success(block())
    } catch (t: Throwable) {
        Result.Failure(DomainError.Unknown(t))
    }
}
