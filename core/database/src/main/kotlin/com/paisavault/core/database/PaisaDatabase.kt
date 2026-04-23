// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.paisavault.core.database.dao.BudgetDao
import com.paisavault.core.database.dao.CategoryDao
import com.paisavault.core.database.dao.TransactionDao
import com.paisavault.core.database.entity.BudgetEntity
import com.paisavault.core.database.entity.CategoryEntity
import com.paisavault.core.database.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PaisaDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        const val NAME = "paisa.db"
    }
}
