// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.data.sms.di

import com.paisavault.core.domain.repository.SmsHistorySource
import com.paisavault.data.sms.ingest.SmsHistorySourceImpl
import com.paisavault.data.sms.parser.SmsParserRule
import com.paisavault.data.sms.parser.rules.AxisRule
import com.paisavault.data.sms.parser.rules.GPayRule
import com.paisavault.data.sms.parser.rules.HdfcRule
import com.paisavault.data.sms.parser.rules.IciciRule
import com.paisavault.data.sms.parser.rules.KotakRule
import com.paisavault.data.sms.parser.rules.PaytmRule
import com.paisavault.data.sms.parser.rules.PhonePeRule
import com.paisavault.data.sms.parser.rules.SbiRule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SmsModule {

    @Binds abstract fun smsHistorySource(impl: SmsHistorySourceImpl): SmsHistorySource

    @Binds @IntoSet abstract fun hdfc(rule: HdfcRule): SmsParserRule
    @Binds @IntoSet abstract fun sbi(rule: SbiRule): SmsParserRule
    @Binds @IntoSet abstract fun icici(rule: IciciRule): SmsParserRule
    @Binds @IntoSet abstract fun axis(rule: AxisRule): SmsParserRule
    @Binds @IntoSet abstract fun kotak(rule: KotakRule): SmsParserRule
    @Binds @IntoSet abstract fun phonePe(rule: PhonePeRule): SmsParserRule
    @Binds @IntoSet abstract fun gpay(rule: GPayRule): SmsParserRule
    @Binds @IntoSet abstract fun paytm(rule: PaytmRule): SmsParserRule
}
