// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.common.error

/**
 * Exhaustive hierarchy of app-layer errors. See BUILD.md § 15.
 * UI maps these to user-facing strings; no raw [Throwable] leaks to the UI layer.
 */
sealed class DomainError(open val cause: Throwable? = null) {

    sealed class Storage : DomainError() {
        data class DatabaseCorrupt(override val cause: Throwable) : Storage()
        data class DiskFull(override val cause: Throwable) : Storage()
        data class EncryptionFailed(override val cause: Throwable) : Storage()
        data class MigrationFailed(val fromVersion: Int, val toVersion: Int, override val cause: Throwable) : Storage()
    }

    sealed class Sms : DomainError() {
        data object PermissionDenied : Sms()
        data class ParseFailed(val smsId: Long) : Sms()
        data object AmbiguousRule : Sms()
    }

    sealed class Ml : DomainError() {
        data object ModelLoadFailed : Ml()
        data class InferenceFailed(override val cause: Throwable) : Ml()
        data object LowConfidence : Ml()
    }

    sealed class Validation : DomainError() {
        data object AmountInvalid : Validation()
        data object CategoryRequired : Validation()
        data object DateInFuture : Validation()
        data class TooLong(val fieldName: String, val maxChars: Int) : Validation()
    }

    sealed class Security : DomainError() {
        data object BiometricUnavailable : Security()
        data object BiometricAuthFailed : Security()
        data object KeystoreUnavailable : Security()
    }

    data class Unknown(override val cause: Throwable) : DomainError(cause)
}
