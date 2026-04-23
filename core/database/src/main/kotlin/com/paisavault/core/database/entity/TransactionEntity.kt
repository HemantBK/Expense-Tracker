// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("date"),
        Index("category_id"),
        Index(value = ["reference"], unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @androidx.room.ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @androidx.room.ColumnInfo(name = "currency") val currency: String,
    @androidx.room.ColumnInfo(name = "type") val type: String, // DEBIT | CREDIT
    @androidx.room.ColumnInfo(name = "merchant") val merchant: String,
    @androidx.room.ColumnInfo(name = "category_id") val categoryId: Long?,
    @androidx.room.ColumnInfo(name = "date") val dateEpochMillis: Long,
    @androidx.room.ColumnInfo(name = "note") val note: String?,
    @androidx.room.ColumnInfo(name = "source") val source: String, // MANUAL | SMS | OCR
    @androidx.room.ColumnInfo(name = "reference") val reference: String?,
    @androidx.room.ColumnInfo(name = "raw_sms_id") val rawSmsId: Long?,
    @androidx.room.ColumnInfo(name = "ml_confidence") val mlConfidence: Float?,
    @androidx.room.ColumnInfo(name = "user_verified") val userVerified: Boolean,
    @androidx.room.ColumnInfo(name = "created_at") val createdAtEpochMillis: Long,
)
