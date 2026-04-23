// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser

import com.paisavault.data.sms.model.SmsMessage

/** A named parser rule. Higher [priority] wins on ambiguous matches. */
interface SmsParserRule {
    val id: String
    val bankName: String
    val priority: Int

    /** Fast sender-side filter. Cheap to run on every SMS. */
    fun matches(sms: SmsMessage): Boolean

    /** Attempt to extract a transaction. Null = structured enough to match but failed to parse. */
    fun parse(sms: SmsMessage): ParsedTxn?
}
