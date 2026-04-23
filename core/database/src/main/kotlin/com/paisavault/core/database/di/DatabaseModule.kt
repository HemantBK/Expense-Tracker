// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.database.di

import android.content.Context
import androidx.room.Room
import com.paisavault.core.database.PaisaDatabase
import com.paisavault.core.database.dao.BudgetDao
import com.paisavault.core.database.dao.CategoryDao
import com.paisavault.core.database.dao.TransactionDao
import com.paisavault.core.security.DatabaseKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePaisaDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider,
    ): PaisaDatabase {
        val passphrase = keyProvider.getPassphrase()
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(context, PaisaDatabase::class.java, PaisaDatabase.NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides fun provideTransactionDao(db: PaisaDatabase): TransactionDao = db.transactionDao()

    @Provides fun provideCategoryDao(db: PaisaDatabase): CategoryDao = db.categoryDao()

    @Provides fun provideBudgetDao(db: PaisaDatabase): BudgetDao = db.budgetDao()
}
