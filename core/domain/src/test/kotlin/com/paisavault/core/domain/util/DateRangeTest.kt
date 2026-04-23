// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.util

import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.Test

class DateRangeTest {

    private val utc = TimeZone.UTC

    @Test
    fun `monthRangeOf returns first and last-millisecond+1 of containing month`() {
        // 2026-04-22T12:00:00Z
        val anchor = Instant.parse("2026-04-22T12:00:00Z")
        val range = monthRangeOf(anchor, zone = utc)

        range.start shouldBe Instant.parse("2026-04-01T00:00:00Z")
        range.end shouldBe Instant.parse("2026-05-01T00:00:00Z")
    }

    @Test
    fun `monthRangeOf handles December correctly`() {
        val anchor = Instant.parse("2026-12-15T09:00:00Z")
        val range = monthRangeOf(anchor, zone = utc)

        range.start shouldBe Instant.parse("2026-12-01T00:00:00Z")
        range.end shouldBe Instant.parse("2027-01-01T00:00:00Z")
    }

    @Test
    fun `weekRangeOf starts on Monday ISO`() {
        // 2026-04-22 is a Wednesday
        val anchor = Instant.parse("2026-04-22T10:00:00Z")
        val range = weekRangeOf(anchor, zone = utc)

        range.start shouldBe Instant.parse("2026-04-20T00:00:00Z")
        range.end shouldBe Instant.parse("2026-04-27T00:00:00Z")
    }
}
