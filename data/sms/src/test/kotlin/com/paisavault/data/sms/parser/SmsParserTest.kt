// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.parser

import com.paisavault.core.domain.model.TransactionType
import com.paisavault.data.sms.model.SmsMessage
import com.paisavault.data.sms.parser.rules.AxisRule
import com.paisavault.data.sms.parser.rules.GPayRule
import com.paisavault.data.sms.parser.rules.HdfcRule
import com.paisavault.data.sms.parser.rules.IciciRule
import com.paisavault.data.sms.parser.rules.KotakRule
import com.paisavault.data.sms.parser.rules.PaytmRule
import com.paisavault.data.sms.parser.rules.PhonePeRule
import com.paisavault.data.sms.parser.rules.SbiRule
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test

class SmsParserTest {

    private val parser = SmsParser(
        setOf(
            HdfcRule(), SbiRule(), IciciRule(), AxisRule(), KotakRule(),
            PhonePeRule(), GPayRule(), PaytmRule(),
        ),
    )

    @Test
    fun `HDFC debit parses amount and merchant`() {
        val sms = SmsMessage(
            id = 1, sender = "VK-HDFCBK",
            body = "Rs.500.00 debited from A/c XX1234 to ZOMATO on 22-04-26. Ref: UPI123456789012",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 50_000L
        parsed.type shouldBe TransactionType.Debit
        parsed.bankName shouldBe "HDFC Bank"
    }

    @Test
    fun `HDFC credit detects credit type`() {
        val sms = SmsMessage(
            id = 2, sender = "AD-HDFCBK",
            body = "INR 5000.00 credited to A/c XX1234 on 20-04-26 by SALARY. Ref No: SAL0001",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.type shouldBe TransactionType.Credit
        parsed.amountMinor shouldBe 500_000L
    }

    @Test
    fun `SBI debit`() {
        val sms = SmsMessage(
            id = 3, sender = "VM-SBIUPI",
            body = "A/c XX1234 debited by Rs 1500.00 on 22-04-26 trf to UPI/PAYEE1. Ref No: SBI123",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 150_000L
        parsed.bankName shouldBe "SBI"
    }

    @Test
    fun `ICICI debit with UPI info`() {
        val sms = SmsMessage(
            id = 4, sender = "VK-ICICIB",
            body = "ICICI Acct XX1234 debited with Rs 250.00 on 22-Apr-26 at SWIGGY. Info: UPI/123456789",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 25_000L
    }

    @Test
    fun `PhonePe paid`() {
        val sms = SmsMessage(
            id = 5, sender = "VK-PHONPE",
            body = "You paid Rs 450.00 to ZOMATO via PhonePe UPI. Txn ID: T2204261234",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.type shouldBe TransactionType.Debit
        parsed.amountMinor shouldBe 45_000L
    }

    @Test
    fun `GPay paid`() {
        val sms = SmsMessage(
            id = 6, sender = "DM-GOOGPY",
            body = "You paid Rs 1200 to AMAZON via Google Pay. UPI transaction ID 123456789012",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 120_000L
    }

    @Test
    fun `Paytm paid`() {
        val sms = SmsMessage(
            id = 7, sender = "VK-PAYTM",
            body = "You have successfully paid Rs 250 to JIO RECHARGE from Paytm Wallet. Order ID: 1234567890",
            receivedAtMillis = 0,
        )
        val parsed = parser.parse(sms)
        parsed shouldNotBe null
        parsed!!.amountMinor shouldBe 25_000L
    }

    @Test
    fun `unknown sender returns null`() {
        val sms = SmsMessage(
            id = 8, sender = "FRIEND",
            body = "Hey, can you send me Rs 500?",
            receivedAtMillis = 0,
        )
        parser.parse(sms) shouldBe null
    }

    @Test
    fun `oversized body rejected`() {
        val sms = SmsMessage(
            id = 9, sender = "VK-HDFCBK",
            body = "A".repeat(10_000),
            receivedAtMillis = 0,
        )
        parser.parse(sms) shouldBe null
    }

    @Test
    fun `non-transaction sms from bank ignored`() {
        val sms = SmsMessage(
            id = 10, sender = "VK-HDFCBK",
            body = "Your OTP for login is 123456. Valid for 3 mins. Do not share with anyone.",
            receivedAtMillis = 0,
        )
        parser.parse(sms) shouldBe null
    }

    @Test
    fun `amount overflow rejected`() {
        val sms = SmsMessage(
            id = 11, sender = "VK-HDFCBK",
            body = "Rs 99999999999999.00 debited from A/c XX1234 at BAD on 22-04-26",
            receivedAtMillis = 0,
        )
        parser.parse(sms) shouldBe null
    }
}
