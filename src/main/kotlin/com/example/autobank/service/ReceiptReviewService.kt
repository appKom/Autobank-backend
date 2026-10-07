package com.example.autobank.service

import com.example.autobank.data.ReceiptReviewRequestBody
import com.example.autobank.data.models.ReceiptReview
import com.example.autobank.data.models.ReceiptStatus
import com.example.autobank.data.receipt.ReceiptReviewResponseBody
import com.example.autobank.repository.receipt.ReceiptRepository
import com.example.autobank.repository.receipt.ReceiptReviewRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ReceiptReviewService(
    private val receiptReviewRepository: ReceiptReviewRepository,
    private val onlineUserService: OnlineUserService,
    private val receiptRepository: ReceiptRepository,
    private val mailService: MailService
) {

    fun createReceiptReview(receiptReview: ReceiptReviewRequestBody): ReceiptReviewResponseBody {
        val onlineuser = onlineUserService.getOnlineUser() ?: throw Exception("User not found")

        if (receiptReview.status != "APPROVED" && receiptReview.status != "DENIED") {
            throw Exception("Invalid status")
        }
        val receipt = receiptRepository.findById(receiptReview.receiptId).orElseThrow { Exception("Receipt not found") }
        val prevReceiptReview = receiptReviewRepository.findFirstByReceiptId(receiptReview.receiptId)
        if (prevReceiptReview != null) {
            println("Updating review")
            receiptReviewRepository.deleteByReceiptId(receiptReview.receiptId)
        }

        val savedReview = receiptReviewRepository.save(ReceiptReview("", receipt, enumValueOf<ReceiptStatus>(receiptReview.status), receiptReview.comment, onlineuser, null))

        val statusText = when (savedReview.status) {
            ReceiptStatus.DENIED -> "Avvist"
            ReceiptStatus.APPROVED -> "Godkjent"
        }

        mailService.sendEmail(
            toEmail = receipt.user.email,
            subject = "Kvitteringen din '${receipt.name}' er ${statusText.lowercase()}",
            htmlBody = """
                <p>Kvitteringen din med navn <strong>${receipt.name}</strong> har en oppdatering.</p>
                
                <p>
                   <strong>Status:</strong> ${statusText}<br>
                   <strong>Kommentar:</strong> ${savedReview.comment}
                </p>
                <p>For flere detaljer, gå inn på: <a href="https://autobank.online.ntnu.no/minside">Autobank</a></p>
            """.trimIndent(),
        )

        return ReceiptReviewResponseBody(
            id = savedReview.id,
            receiptId = savedReview.receipt.id,
            status = savedReview.status,
            comment = savedReview.comment,
            onlineUserId = savedReview.user.id,
            createdAt = savedReview.createdat
        )

    }
}