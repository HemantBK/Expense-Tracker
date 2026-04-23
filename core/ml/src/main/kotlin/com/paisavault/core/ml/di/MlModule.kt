// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.ml.di

import com.paisavault.core.domain.classifier.MerchantClassifier
import com.paisavault.core.ml.CompositeCategorizer
import com.paisavault.core.ml.MerchantCategorizer
import com.paisavault.core.ml.adapter.MerchantClassifierAdapter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MlModule {

    @Binds
    abstract fun bindMerchantCategorizer(impl: CompositeCategorizer): MerchantCategorizer

    @Binds
    abstract fun bindMerchantClassifier(impl: MerchantClassifierAdapter): MerchantClassifier
}
