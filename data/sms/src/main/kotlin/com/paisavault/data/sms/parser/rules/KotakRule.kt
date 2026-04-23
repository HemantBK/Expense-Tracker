// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser.rules

import javax.inject.Inject

internal class KotakRule @Inject constructor() : GenericBankRule(
    id = "kotak",
    bankName = "Kotak Bank",
    priority = 100,
    senderCodes = listOf("KOTAKB", "KOTAK"),
)
