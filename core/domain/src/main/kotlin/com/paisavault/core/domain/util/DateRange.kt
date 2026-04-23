// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.util

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DatePeriod

data class DateRange(val start: Instant, val end: Instant) {
    init {
        require(start <= end) { "start must be <= end" }
    }
}

/** Month range containing [instant], in the given [zone]. Start = 00:00 of day 1; end = start of next month. */
fun monthRangeOf(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): DateRange {
    val ldt: LocalDateTime = instant.toLocalDateTime(zone)
    val firstOfMonth = kotlinx.datetime.LocalDate(ldt.year, ldt.monthNumber, 1)
    val start = firstOfMonth.atStartOfDayIn(zone)
    val nextMonthFirst = firstOfMonth.plus(DatePeriod(months = 1))
    val end = nextMonthFirst.atStartOfDayIn(zone)
    return DateRange(start, end)
}

/** Week range containing [instant]. Week starts Monday per ISO 8601. */
fun weekRangeOf(instant: Instant, zone: TimeZone = TimeZone.currentSystemDefault()): DateRange {
    val ldt = instant.toLocalDateTime(zone)
    val daysFromMonday = ((ldt.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber) + DAYS_PER_WEEK) % DAYS_PER_WEEK
    val weekStart = kotlinx.datetime.LocalDate(ldt.year, ldt.monthNumber, ldt.dayOfMonth)
        .minus(DatePeriod(days = daysFromMonday))
    val start = weekStart.atStartOfDayIn(zone)
    val end = weekStart.plus(DatePeriod(days = DAYS_PER_WEEK)).atStartOfDayIn(zone)
    return DateRange(start, end)
}

private const val DAYS_PER_WEEK = 7

private val DayOfWeek.isoDayNumber: Int get() = isoDayOfWeek

private val DayOfWeek.isoDayOfWeek: Int
    get() = when (this) {
        DayOfWeek.MONDAY -> 1
        DayOfWeek.TUESDAY -> 2
        DayOfWeek.WEDNESDAY -> 3
        DayOfWeek.THURSDAY -> 4
        DayOfWeek.FRIDAY -> 5
        DayOfWeek.SATURDAY -> 6
        DayOfWeek.SUNDAY -> 7
    }

private fun kotlinx.datetime.LocalDate.minus(period: DatePeriod): kotlinx.datetime.LocalDate =
    this.plus(DatePeriod(years = -period.years, months = -period.months, days = -period.days))
