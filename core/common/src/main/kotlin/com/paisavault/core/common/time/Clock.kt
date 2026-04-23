// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.common.time

import kotlinx.datetime.Instant

/**
 * Time source abstraction. Inject instead of calling [Instant.fromEpochMilliseconds] /
 * [System.currentTimeMillis] directly — makes tests deterministic.
 */
fun interface Clock {
    fun now(): Instant

    companion object {
        val System: Clock = Clock { Instant.fromEpochMilliseconds(java.lang.System.currentTimeMillis()) }
    }
}
