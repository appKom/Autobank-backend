package com.example.autobank.service

import com.example.autobank.data.models.*
import com.example.autobank.data.receipt.*
import com.example.autobank.repository.receipt.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class ReceiptAdminServiceTest : FunSpec({

    val receiptInfoRepository = mockk<ReceiptInfoRepositoryImpl>()
    val receiptService = mockk<ReceiptService>()
    
    val receiptAdminService = ReceiptAdminService(receiptInfoRepository, receiptService)

    context("getAll") {
        test("should return all receipts with pagination") {
            // Given
            val mockReceiptInfo = mockk<ReceiptInfo> {
                every { receiptId } returns "receipt-1"
                every { amount } returns 1000.0
                every { receiptName } returns "Test Receipt"
                every { receiptDescription } returns "Test Description"
                every { receiptCreatedAt } returns LocalDateTime.now()
                every { committeeName } returns "Test Committee"
                every { userFullname } returns "Test User"
                every { cardNumber } returns "1234-5678-9012-3456"
                every { accountNumber } returns null
                every { attachmentCount } returns 2L
                every { latestReviewStatus } returns null
                every { latestReviewCreatedAt } returns null
                every { latestReviewComment } returns null
            }
            
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns listOf(mockReceiptInfo)
                every { totalElements } returns 1L
            }

            // When
            val result = receiptAdminService.getAll(0, 10, null, null, null, null, null)

            // Then
            result shouldNotBe null
            result?.receipts?.size shouldBe 1
            result?.total shouldBe 1L
            verify { receiptInfoRepository.findAll(any(), any()) }
        }

        test("should return empty list when no receipts found") {
            // Given
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptAdminService.getAll(0, 10, null, null, null, null, null)

            // Then
            result shouldNotBe null
            result?.receipts?.size shouldBe 0
            result?.total shouldBe 0L
        }

        test("should apply sorting when sortField is provided") {
            // Given
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptAdminService.getAll(0, 10, null, null, null, "amount", "DESC")

            // Then
            result shouldNotBe null
            verify { receiptInfoRepository.findAll(any(), any()) }
        }

        test("should apply default sorting when sortField is null") {
            // Given
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptAdminService.getAll(0, 10, null, null, null, null, null)

            // Then
            result shouldNotBe null
            verify { receiptInfoRepository.findAll(any(), any()) }
        }
    }

    context("getReceipt") {
        test("should return complete receipt when found") {
            // Given
            val mockReceiptInfo = mockk<ReceiptInfo> {
                every { receiptId } returns "receipt-1"
                every { amount } returns 1000.0
                every { receiptName } returns "Test Receipt"
                every { receiptDescription } returns "Test Description"
                every { receiptCreatedAt } returns LocalDateTime.now()
                every { committeeName } returns "Test Committee"
                every { userFullname } returns "Test User"
                every { cardNumber } returns "1234-5678-9012-3456"
                every { accountNumber } returns null
                every { attachmentCount } returns 2L
                every { latestReviewStatus } returns null
                every { latestReviewCreatedAt } returns null
                every { latestReviewComment } returns null
            }
            
            val mockCompleteReceipt = mockk<CompleteReceipt>()
            
            every { receiptInfoRepository.findById("receipt-1") } returns mockReceiptInfo
            every { receiptService.getCompleteReceipt(mockReceiptInfo) } returns mockCompleteReceipt

            // When
            val result = receiptAdminService.getReceipt("receipt-1")

            // Then
            result shouldNotBe null
            result shouldBe mockCompleteReceipt
            verify { receiptInfoRepository.findById("receipt-1") }
            verify { receiptService.getCompleteReceipt(mockReceiptInfo) }
        }

        test("should return null when receipt not found") {
            // Given
            every { receiptInfoRepository.findById("receipt-999") } returns null

            // When
            val result = receiptAdminService.getReceipt("receipt-999")

            // Then
            result shouldBe null
            verify { receiptInfoRepository.findById("receipt-999") }
        }
    }
})
