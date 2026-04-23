// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.security.di

import com.paisavault.core.domain.security.Encryptor
import com.paisavault.core.security.passphrase.PassphraseEncryptor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SecurityModule {

    @Binds
    abstract fun bindEncryptor(impl: PassphraseEncryptor): Encryptor
}
