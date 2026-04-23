// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (c) 2026 PaisaVault contributors

package com.paisavault.core.domain.usecase

import com.paisavault.core.common.error.DomainError
import com.paisavault.core.common.result.Result
import com.paisavault.core.common.time.Clock
import com.paisavault.core.domain.model.Transaction
import com.paisavault.core.domain.repository.CategorySpend
import com.paisavault.core.domain.repository.TransactionRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.Test

class AddTransactionUseCaseTest {

    private val fixedNow = Instant.parse("2026-04-22T12:00:00Z")
    private val clock = Clock { fixedNow }
    private val repository = FakeRepository()
    private val useCase = AddTransactionUseCase(repository, clock)

    @Test
    fun `rejects zero amount`() = runBlocking {
        val result = useCase(
            AddTransactionUseCase.Input(
                amountMinor = 0,
                merchant = "Zomato",
                categoryId = 1,
                date = fixedNow,
            ),
        )
        result.shouldBeInstanceOf<Result.Failure>()
        result.error.shouldBeInstanceOf<DomainError.Validation.AmountInvalid>()
    }

    @Test
    fun `rejects future date`() = runBlocking {
        val future = Instant.parse("2030-01-01T00:00:00Z")
        val result = useCase(
            AddTransactionUseCase.Input(
                amountMinor = 1_000,
                merchant = "Zomato",
                categoryId = 1,
                date = future,
            ),
        )
        result.shouldBeInstanceOf<Result.Failure>()
    }

    @Test
    fun `rejects blank merchant`() = runBlocking {
        val result = useCase(
            AddTransactionUseCase.Input(
                amountMinor = 1_000,
                merchant = "   ",
                categoryId = 1,
                date = fixedNow,
            ),
        )
        result.shouldBeInstanceOf<Result.Failure>()
    }

    @Test
    fun `accepts valid input and delegates to repo`() = runBlocking {
        val result = useCase(
            AddTransactionUseCase.Input(
                amountMinor = 12_550,
                merchant = "Zomato",
                categoryId = 1,
                date = fixedNow,
                note = "Lunch",
            ),
        )
        result.shouldBeInstanceOf<Result.Success<Long>>()
        repository.inserted.size shouldBe 1
        repository.inserted.first().merchant shouldBe "Zomato"
    }

    private class FakeRepository : TransactionRepository {
        val inserted = mutableListOf<Transaction>()
        override fun observeInRange(start: Instant, end: Instant): Flow<List<Transaction>> = flowOf(emptyList())
        override fun observeTotalDebitMinor(start: Instant, end: Instant): Flow<Long> = flowOf(0L)
        override fun observeCategoryBreakdown(start: Instant, end: Instant): Flow<List<CategorySpend>> = flowOf(emptyList())
        override suspend fun add(txn: Transaction): Result<Long> {
            inserted += txn
            return Result.Success(inserted.size.toLong())
        }
        override suspend fun update(txn: Transaction): Result<Unit> = Result.Success(Unit)
        override suspend fun delete(id: Long): Result<Unit> = Result.Success(Unit)
    }
}
