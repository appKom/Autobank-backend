package com.example.autobank.service

import com.example.autobank.data.models.Attachment
import com.example.autobank.data.models.Committee
import com.example.autobank.data.models.Receipt
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.receipt.AttachmentRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime

class AttachmentServiceTest : FunSpec({

    val attachmentRepository = mockk<AttachmentRepository>()
    val attachmentService = AttachmentService(attachmentRepository)

    context("createAttachment") {
        test("should create attachment successfully") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                "receipt-1", 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now()
            )
            val attachment = Attachment(
                "attachment-1", mockReceipt, "test-file.jpg"
            )
            
            every { attachmentRepository.save(attachment) } returns attachment

            // When
            val result = attachmentService.createAttachment(attachment)

            // Then
            result shouldNotBe null
            result shouldBe attachment
            result.id shouldBe "attachment-1"
            result.name shouldBe "test-file.jpg"
            verify { attachmentRepository.save(attachment) }
        }
    }

    context("getAttachmentsByReceiptId") {
        test("should return attachments for existing receipt") {
            // Given
            val receiptId = "receipt-1"
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = com.example.autobank.data.models.Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                receiptId, 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now()
            )
            val attachments = listOf(
                Attachment("attachment-1", mockReceipt, "file1.jpg"),
                Attachment("attachment-2", mockReceipt, "file2.pdf")
            )
            
            every { attachmentRepository.findByReceiptId(receiptId) } returns attachments

            // When
            val result = attachmentService.getAttachmentsByReceiptId(receiptId)

            // Then
            result shouldNotBe null
            result.size shouldBe 2
            result[0].name shouldBe "file1.jpg"
            result[1].name shouldBe "file2.pdf"
            verify { attachmentRepository.findByReceiptId(receiptId) }
        }

        test("should return empty list for receipt with no attachments") {
            // Given
            val receiptId = "receipt-without-attachments"
            every { attachmentRepository.findByReceiptId(receiptId) } returns emptyList()

            // When
            val result = attachmentService.getAttachmentsByReceiptId(receiptId)

            // Then
            result shouldNotBe null
            result shouldBe emptyList()
            verify { attachmentRepository.findByReceiptId(receiptId) }
        }
    }
})
