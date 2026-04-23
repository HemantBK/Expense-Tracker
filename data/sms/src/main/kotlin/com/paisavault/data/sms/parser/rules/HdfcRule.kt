// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser.rules

import javax.inject.Inject

internal class HdfcRule @Inject constructor() : GenericBankRule(
    id = "hdfc",
    bankName = "HDFC Bank",
    priority = 100,
    senderCodes = listOf("HDFCBK", "HDFCBN"),
)
