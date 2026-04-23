// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.transaction.di

import com.paisavault.core.domain.repository.ExportSink
import com.paisavault.core.domain.repository.TransactionRepository
import com.paisavault.data.transaction.TransactionRepositoryImpl
import com.paisavault.data.transaction.export.SafExportSink
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TransactionDataModule {

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindExportSink(impl: SafExportSink): ExportSink
}
