// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.paisavault.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT * FROM transactions
        WHERE date BETWEEN :startMillis AND :endMillis
        ORDER BY date DESC, id DESC
        """,
    )
    fun observeInRange(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount_minor), 0) FROM transactions
        WHERE type = 'DEBIT' AND date BETWEEN :startMillis AND :endMillis
        """,
    )
    fun observeTotalDebit(startMillis: Long, endMillis: Long): Flow<Long>

    @Query(
        """
        SELECT category_id AS categoryId, SUM(amount_minor) AS totalMinor
        FROM transactions
        WHERE type = 'DEBIT' AND date BETWEEN :startMillis AND :endMillis
        GROUP BY category_id
        """,
    )
    fun observeCategoryBreakdown(startMillis: Long, endMillis: Long): Flow<List<CategorySpendRow>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions
        WHERE source = 'SMS' AND user_verified = 0
        ORDER BY date DESC, id DESC
        """,
    )
    fun observePendingSms(): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE source = 'SMS' AND user_verified = 0")
    fun observePendingSmsCount(): Flow<Int>

    @Query("UPDATE transactions SET user_verified = 1 WHERE id = :id")
    suspend fun markVerified(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(txn: TransactionEntity): Long

    @Update
    suspend fun update(txn: TransactionEntity)

    @Delete
    suspend fun delete(txn: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class CategorySpendRow(val categoryId: Long?, val totalMinor: Long)
