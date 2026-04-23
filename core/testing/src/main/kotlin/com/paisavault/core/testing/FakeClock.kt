// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.testing

import com.paisavault.core.common.time.Clock
import kotlinx.datetime.Instant

/** Deterministic clock for tests. */
class FakeClock(initial: Instant = Instant.fromEpochMilliseconds(0)) : Clock {
    private var current: Instant = initial
    override fun now(): Instant = current
    fun advance(durationMillis: Long) {
        current = Instant.fromEpochMilliseconds(current.toEpochMilliseconds() + durationMillis)
    }
    fun set(instant: Instant) {
        current = instant
    }
}
