// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser.rules

import javax.inject.Inject

internal class SbiRule @Inject constructor() : GenericBankRule(
    id = "sbi",
    bankName = "SBI",
    priority = 100,
    senderCodes = listOf("SBIINB", "SBIUPI", "SBIPSG", "SBICRD"),
)
