// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.common.result

import com.paisavault.core.common.error.DomainError

/**
 * Ops that can fail return [Result]. Never throw across a domain boundary.
 */
sealed interface Result<out T> {
    data class Success<T>(val value: T) : Result<T>
    data class Failure(val error: DomainError) : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(value))
        is Result.Failure -> this
    }

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    when (this) {
        is Result.Success -> transform(value)
        is Result.Failure -> this
    }

inline fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(value)
    return this
}

inline fun <T> Result<T>.onFailure(block: (DomainError) -> Unit): Result<T> {
    if (this is Result.Failure) block(error)
    return this
}

fun <T> T.asSuccess(): Result<T> = Result.Success(this)

fun DomainError.asFailure(): Result<Nothing> = Result.Failure(this)
