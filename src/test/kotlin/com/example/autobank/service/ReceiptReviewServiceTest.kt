package com.example.autobank.service

import com.example.autobank.data.ReceiptReviewRequestBody
import com.example.autobank.data.models.*
import com.example.autobank.data.receipt.ReceiptReviewResponseBody
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.receipt.ReceiptRepository
import com.example.autobank.repository.receipt.ReceiptReviewRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.*

class ReceiptReviewServiceTest : FunSpec({

    val receiptReviewRepository = mockk<ReceiptReviewRepository>()
    val onlineUserService = mockk<OnlineUserService>()
    val receiptRepository = mockk<ReceiptRepository>()
    
    val receiptReviewService = ReceiptReviewService(receiptReviewRepository, onlineUserService, receiptRepository)

    context("createReceiptReview") {
        test("should create new receipt review successfully") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                "receipt-1", 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now()
            )
            val mockReceiptReview = ReceiptReview(
                "review-1", mockReceipt, ReceiptStatus.APPROVED, "Good receipt", mockUser, LocalDateTime.now()
            )
            
            val receiptReviewRequest = ReceiptReviewRequestBody(
                receiptId = "receipt-1",
                status = "APPROVED",
                comment = "Good receipt"
            )
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptRepository.findById("receipt-1") } returns Optional.of(mockReceipt)
            every { receiptReviewRepository.findFirstByReceiptId("receipt-1") } returns null
            every { receiptReviewRepository.save(any()) } returns mockReceiptReview

            // When
            val result = receiptReviewService.createReceiptReview(receiptReviewRequest)

            // Then
            result shouldNotBe null
            result.id shouldBe "review-1"
            result.receiptId shouldBe "receipt-1"
            result.status shouldBe ReceiptStatus.APPROVED
            result.comment shouldBe "Good receipt"
            result.onlineUserId shouldBe "1"
            verify { onlineUserService.getOnlineUser() }
            verify { receiptRepository.findById("receipt-1") }
            verify { receiptReviewRepository.save(any()) }
        }

        test("should update existing receipt review") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                "receipt-1", 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now()
            )
            val existingReview = ReceiptReview(
                "old-review-1", mockReceipt, ReceiptStatus.DENIED, "Old comment", mockUser, LocalDateTime.now()
            )
            val newReview = ReceiptReview(
                "review-1", mockReceipt, ReceiptStatus.APPROVED, "Updated comment", mockUser, LocalDateTime.now()
            )
            
            val receiptReviewRequest = ReceiptReviewRequestBody(
                receiptId = "receipt-1",
                status = "APPROVED",
                comment = "Updated comment"
            )
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptRepository.findById("receipt-1") } returns Optional.of(mockReceipt)
            every { receiptReviewRepository.findFirstByReceiptId("receipt-1") } returns existingReview
            every { receiptReviewRepository.deleteByReceiptId("receipt-1") } returns Unit
            every { receiptReviewRepository.save(any()) } returns newReview

            // When
            val result = receiptReviewService.createReceiptReview(receiptReviewRequest)

            // Then
            result shouldNotBe null
            result.status shouldBe ReceiptStatus.APPROVED
            result.comment shouldBe "Updated comment"
            verify { receiptReviewRepository.deleteByReceiptId("receipt-1") }
            verify { receiptReviewRepository.save(any()) }
        }

        test("should throw Exception when user not found") {
            // Given
            val receiptReviewRequest = ReceiptReviewRequestBody(
                receiptId = "receipt-1",
                status = "APPROVED",
                comment = "Good receipt"
            )
            
            every { onlineUserService.getOnlineUser() } returns null

            // When & Then
            val exception = shouldThrow<Exception> { receiptReviewService.createReceiptReview(receiptReviewRequest) }
            exception.message shouldBe "User not found"
        }

        test("should throw Exception when receipt not found") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptReviewRequest = ReceiptReviewRequestBody(
                receiptId = "receipt-999",
                status = "APPROVED",
                comment = "Good receipt"
            )
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptRepository.findById("receipt-999") } returns Optional.empty()

            // When & Then
            val exception = shouldThrow<Exception> { receiptReviewService.createReceiptReview(receiptReviewRequest) }
            exception.message shouldBe "Receipt not found"
        }

        test("should throw Exception when status is invalid") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptReviewRequest = ReceiptReviewRequestBody(
                receiptId = "receipt-1",
                status = "INVALID_STATUS",
                comment = "Good receipt"
            )
            
            every { onlineUserService.getOnlineUser() } returns mockUser

            // When & Then
            val exception = shouldThrow<Exception> { receiptReviewService.createReceiptReview(receiptReviewRequest) }
            exception.message shouldBe "Invalid status"
        }
    }
})
