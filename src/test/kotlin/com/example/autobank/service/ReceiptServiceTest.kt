package com.example.autobank.service

import com.example.autobank.data.models.*
import com.example.autobank.data.receipt.*
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.receipt.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

class ReceiptServiceTest : FunSpec({

    val onlineUserService = mockk<OnlineUserService>()
    val receiptRepository = mockk<ReceiptRepository>()
    val blobService = mockk<BlobService>()
    val attachmentService = mockk<AttachmentService>()
    val committeeService = mockk<CommitteeService>()
    val receiptInfoRepository = mockk<ReceiptInfoRepositoryImpl>()
    
    val receiptService = ReceiptService(
        onlineUserService,
        receiptRepository,
        blobService,
        attachmentService,
        committeeService,
        receiptInfoRepository
    )

    context("createReceipt") {
        test("should create receipt successfully with card number") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                "receipt-1", 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now(),
                card_number = "1234-5678-9012-3456",
                account_number = null
            )
            val mockAttachment = Attachment("attachment-1", mockReceipt, "test-file.jpg")
            
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns "1234-5678-9012-3456"
                    every { accountnumber } returns null
                    every { usedOnlineCard } returns true
                }
                every { attachments } returns arrayOf("base64-image-data")
            }
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { committeeService.getCommitteeById("1") } returns mockCommittee
            every { receiptRepository.save(any()) } returns mockReceipt
            every { blobService.uploadFile("base64-image-data") } returns "test-file.jpg"
            every { attachmentService.createAttachment(any()) } returns mockAttachment

            // When
            val result = receiptService.createReceipt(receiptRequestBody)

            // Then
            result shouldNotBe null
            verify { onlineUserService.getOnlineUser() }
            verify { committeeService.getCommitteeById("1") }
            verify { receiptRepository.save(any()) }
            verify { blobService.uploadFile("base64-image-data") }
            verify { attachmentService.createAttachment(any()) }
        }

        test("should create receipt successfully with account number") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                "receipt-1", 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now(),
                card_number = null,
                account_number = "12345678901"
            )
            
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns null
                    every { accountnumber } returns "12345678901"
                    every { usedOnlineCard } returns false
                }
                every { attachments } returns arrayOf()
            }
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { committeeService.getCommitteeById("1") } returns mockCommittee
            every { receiptRepository.save(any()) } returns mockReceipt

            // When
            val result = receiptService.createReceipt(receiptRequestBody)

            // Then
            result shouldNotBe null
            verify { onlineUserService.getOnlineUser() }
            verify { committeeService.getCommitteeById("1") }
            verify { receiptRepository.save(any()) }
        }

        test("should throw Exception when user not found") {
            // Given
            val receiptRequestBody = mockk<ReceiptRequestBody>()
            every { onlineUserService.getOnlineUser() } returns null

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "User not found"
            verify { onlineUserService.getOnlineUser() }
        }

        test("should throw Exception when receipt not sent") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns null
            }
            every { onlineUserService.getOnlineUser() } returns mockUser

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Receipt not sent"
            verify { onlineUserService.getOnlineUser() }
        }

        test("should throw Exception when no payment information provided") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns null
                    every { accountnumber } returns null
                    every { usedOnlineCard } returns false
                }
            }
            every { onlineUserService.getOnlineUser() } returns mockUser

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Card number or account number must be provided"
            verify { onlineUserService.getOnlineUser() }
        }

        test("should throw Exception when both card and account number provided") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns "1234-5678-9012-3456"
                    every { accountnumber } returns "12345678901"
                    every { usedOnlineCard } returns true
                }
            }
            every { onlineUserService.getOnlineUser() } returns mockUser

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Card and account number can not both be provided"
            verify { onlineUserService.getOnlineUser() }
        }

        test("should throw Exception when committee ID not provided") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns null
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns "1234-5678-9012-3456"
                    every { accountnumber } returns null
                    every { usedOnlineCard } returns true
                }
            }
            every { onlineUserService.getOnlineUser() } returns mockUser

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Committee ID not provided"
            verify { onlineUserService.getOnlineUser() }
        }

        test("should throw Exception when committee not found") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns "1234-5678-9012-3456"
                    every { accountnumber } returns null
                    every { usedOnlineCard } returns true
                }
            }
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { committeeService.getCommitteeById("1") } returns null

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Committee not found"
            verify { onlineUserService.getOnlineUser() }
            verify { committeeService.getCommitteeById("1") }
        }

        test("should throw Exception when amount not provided") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns null
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns "1234-5678-9012-3456"
                    every { accountnumber } returns null
                    every { usedOnlineCard } returns true
                }
            }
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { committeeService.getCommitteeById("1") } returns mockCommittee

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Amount not provided"
            verify { onlineUserService.getOnlineUser() }
            verify { committeeService.getCommitteeById("1") }
        }

        test("should handle attachment upload failure and rollback") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockCommittee = Committee("1", "Test Committee")
            val mockReceipt = Receipt(
                "receipt-1", 1000.0, mockCommittee, "Test Receipt", "Test Description",
                mockUser, emptySet(), emptySet(), LocalDateTime.now(),
                card_number = "1234-5678-9012-3456",
                account_number = null
            )
            
            val receiptRequestBody = mockk<ReceiptRequestBody> {
                every { receipt } returns mockk<ReceiptDTO> {
                    every { amount } returns 1000.0
                    every { name } returns "Test Receipt"
                    every { description } returns "Test Description"
                    every { committee_id } returns "1"
                }
                every { receiptPaymentInformation } returns mockk<ReceiptPaymentInformation> {
                    every { cardnumber } returns "1234-5678-9012-3456"
                    every { accountnumber } returns null
                    every { usedOnlineCard } returns true
                }
                every { attachments } returns arrayOf("invalid-base64-data")
            }
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { committeeService.getCommitteeById("1") } returns mockCommittee
            every { receiptRepository.save(any()) } returns mockReceipt
            every { blobService.uploadFile("invalid-base64-data") } throws Exception("Invalid file format")
            every { receiptRepository.delete(mockReceipt) } returns Unit

            // When & Then
            val exception = assertThrows<Exception> { receiptService.createReceipt(receiptRequestBody) }
            exception.message shouldBe "Invalid file format"
            verify { onlineUserService.getOnlineUser() }
            verify { committeeService.getCommitteeById("1") }
            verify { receiptRepository.save(any()) }
            verify { blobService.uploadFile("invalid-base64-data") }
            verify { receiptRepository.delete(mockReceipt) }
        }
    }
    
    context("getAllReceiptsFromUser") {
        test("should return all receipts for user") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptService.getAllReceiptsFromUser(0, 10, null, null, null, null, null)

            // Then
            result shouldNotBe null
            result?.receipts shouldBe emptyArray()
            result?.total shouldBe 0L
            verify { onlineUserService.getOnlineUser() }
        }

        test("should return receipts with pagination") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
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
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns listOf(mockReceiptInfo)
                every { totalElements } returns 1L
            }

            // When
            val result = receiptService.getAllReceiptsFromUser(0, 10, null, null, null, null, null)

            // Then
            result shouldNotBe null
            result?.receipts?.size shouldBe 1
            result?.total shouldBe 1L
        }

        test("should apply sorting when sortField is provided") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptService.getAllReceiptsFromUser(0, 10, null, null, null, "amount", "DESC")

            // Then
            result shouldNotBe null
            verify { receiptInfoRepository.findAll(any(), any()) }
        }

        test("should apply default sorting when sortField is null") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptService.getAllReceiptsFromUser(0, 10, null, null, null, null, null)

            // Then
            result shouldNotBe null
            verify { receiptInfoRepository.findAll(any(), any()) }
        }

        test("should apply filters when provided") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findAll(any(), any()) } returns mockk {
                every { toList() } returns emptyList()
                every { totalElements } returns 0L
            }

            // When
            val result = receiptService.getAllReceiptsFromUser(0, 10, "APPROVED", "Test Committee", "search term", null, null)

            // Then
            result shouldNotBe null
            verify { receiptInfoRepository.findAll(any(), any()) }
        }

        test("should throw Exception when user is not found") {
            // Given
            every { onlineUserService.getOnlineUser() } returns null

            // When & Then
            val exception = assertThrows<Exception> { receiptService.getAllReceiptsFromUser(0, 10, null, null, null, null, null) }
            exception.message shouldBe "User not found"
        }
    }

    context("getReceipt") {
        test("should return existing receipt") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockReceiptInfo = mockk<ReceiptInfo> {
                every { receiptId } returns "receipt-1"
                every { userId } returns "1"
                every { amount } returns 1000.0
                every { receiptName } returns "Test Receipt"
                every { receiptDescription } returns "Test Description"
                every { receiptCreatedAt } returns LocalDateTime.now()
                every { committeeName } returns "Test Committee"
                every { userFullname } returns "Test User"
                every { cardNumber } returns "1234-5678-9012-3456"
                every { accountNumber } returns null
                every { attachmentCount } returns 0L
                every { latestReviewStatus } returns null
                every { latestReviewCreatedAt } returns null
                every { latestReviewComment } returns null
            }
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findById("receipt-1") } returns mockReceiptInfo
            every { attachmentService.getAttachmentsByReceiptId("receipt-1") } returns emptyList()
            every { blobService.downloadImage(any()) } returns "test-file.jpg"

            // When
            val result = receiptService.getReceipt("receipt-1")

            // Then
            result shouldNotBe null
            verify { onlineUserService.getOnlineUser() }
            verify { receiptInfoRepository.findById("receipt-1") }
        }

        test("should throw Exception when user is not found") {
            // Given
            every { onlineUserService.getOnlineUser() } returns null

            // When & Then
            val exception = assertThrows<Exception> { receiptService.getReceipt("receipt-1") }
            exception.message shouldBe "User not found"
        }

        test("should return null for another user's receipt") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockReceiptInfo = mockk<ReceiptInfo> {
                every { receiptId } returns "receipt-1"
                every { userId } returns "2" // Different user
                every { amount } returns 1000.0
                every { receiptName } returns "Test Receipt"
                every { receiptDescription } returns "Test Description"
                every { receiptCreatedAt } returns LocalDateTime.now()
                every { committeeName } returns "Test Committee"
                every { userFullname } returns "Test User"
                every { cardNumber } returns "1234-5678-9012-3456"
                every { accountNumber } returns null
                every { attachmentCount } returns 0L
                every { latestReviewStatus } returns null
                every { latestReviewCreatedAt } returns null
                every { latestReviewComment } returns null
            }
            
            every { onlineUserService.getOnlineUser() } returns mockUser
            every { receiptInfoRepository.findById("receipt-1") } returns mockReceiptInfo

            // When
            val result = receiptService.getReceipt("receipt-1")

            // Then
            result shouldBe null
        }
    }

    context("getCompleteReceipt") {
        test("should return complete receipt with attachments") {
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
            
            val mockAttachment = mockk<Attachment> {
                every { name } returns "test-file.jpg"
            }
            
            every { attachmentService.getAttachmentsByReceiptId("receipt-1") } returns listOf(mockAttachment)
            every { blobService.downloadImage("test-file.jpg") } returns "base64data"

            // When
            val result = receiptService.getCompleteReceipt(mockReceiptInfo)

            // Then
            result shouldNotBe null
            result.receiptId shouldBe "receipt-1"
            result.amount shouldBe 1000.0
            result.receiptName shouldBe "Test Receipt"
            result.attachmentCount shouldBe 2
            verify { attachmentService.getAttachmentsByReceiptId("receipt-1") }
            verify { blobService.downloadImage("test-file.jpg") }
        }

        test("should return complete receipt with account number") {
            // Given
            val mockReceiptInfo = mockk<ReceiptInfo> {
                every { receiptId } returns "receipt-1"
                every { amount } returns 1000.0
                every { receiptName } returns "Test Receipt"
                every { receiptDescription } returns "Test Description"
                every { receiptCreatedAt } returns LocalDateTime.now()
                every { committeeName } returns "Test Committee"
                every { userFullname } returns "Test User"
                every { cardNumber } returns null
                every { accountNumber } returns "12345678901"
                every { attachmentCount } returns 0L
                every { latestReviewStatus } returns null
                every { latestReviewCreatedAt } returns null
                every { latestReviewComment } returns null
            }
            
            every { attachmentService.getAttachmentsByReceiptId("receipt-1") } returns emptyList()

            // When
            val result = receiptService.getCompleteReceipt(mockReceiptInfo)

            // Then
            result shouldNotBe null
            result.paymentOrCard shouldBe "Payment"
            result.paymentAccountNumber shouldBe "12345678901"
            result.cardCardNumber shouldBe ""
        }

        test("should return complete receipt with card number") {
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
                every { attachmentCount } returns 0L
                every { latestReviewStatus } returns null
                every { latestReviewCreatedAt } returns null
                every { latestReviewComment } returns null
            }
            
            every { attachmentService.getAttachmentsByReceiptId("receipt-1") } returns emptyList()

            // When
            val result = receiptService.getCompleteReceipt(mockReceiptInfo)

            // Then
            result shouldNotBe null
            result.paymentOrCard shouldBe "Card"
            result.paymentAccountNumber shouldBe ""
            result.cardCardNumber shouldBe "1234-5678-9012-3456"
        }

        test("should handle multiple attachments") {
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
            
            val mockAttachment1 = mockk<Attachment> {
                every { name } returns "test-file1.jpg"
            }
            val mockAttachment2 = mockk<Attachment> {
                every { name } returns "test-file2.pdf"
            }
            
            every { attachmentService.getAttachmentsByReceiptId("receipt-1") } returns listOf(mockAttachment1, mockAttachment2)
            every { blobService.downloadImage("test-file1.jpg") } returns "base64data1"
            every { blobService.downloadImage("test-file2.pdf") } returns "base64data2"

            // When
            val result = receiptService.getCompleteReceipt(mockReceiptInfo)

            // Then
            result shouldNotBe null
            result.attachments.size shouldBe 2
            verify { blobService.downloadImage("test-file1.jpg") }
            verify { blobService.downloadImage("test-file2.pdf") }
        }
    }
})