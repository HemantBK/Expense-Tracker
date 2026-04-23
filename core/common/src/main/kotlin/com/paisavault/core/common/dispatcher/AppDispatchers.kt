// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.common.dispatcher

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Injected dispatcher bundle. Inject this instead of calling [kotlinx.coroutines.Dispatchers]
 * directly — makes tests deterministic.
 */
data class AppDispatchers(
    val main: CoroutineDispatcher,
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
)

enum class Dispatcher { Main, Io, Default }
