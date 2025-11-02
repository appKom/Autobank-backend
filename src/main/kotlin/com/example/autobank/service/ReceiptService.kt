package com.example.autobank.service

import com.example.autobank.data.models.*
import com.example.autobank.data.receipt.*
import com.example.autobank.data.receipt.ReceiptInfoResponseBody
import com.example.autobank.repository.receipt.*
import com.example.autobank.repository.receipt.specification.ReceiptInfoViewSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import com.example.autobank.service.MailService

@Service

class ReceiptService {

    @Autowired
    lateinit var onlineUserService: OnlineUserService

    @Autowired
    lateinit var receiptRepository: ReceiptRepository

    @Autowired
    lateinit var blobService: BlobService

    @Autowired
    lateinit var attachmentService: AttachmentService

    @Autowired
    lateinit var committeeService: CommitteeService
    @Autowired
    lateinit var mailService: MailService
    @Autowired
    lateinit var receiptInfoRepository: ReceiptInfoRepositoryImpl


    fun createReceipt(receiptRequestBody: ReceiptRequestBody): ReceiptResponseBody {
        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")
        val receiptinfo = receiptRequestBody.receipt ?: throw Exception("Receipt not sent")


        val cardnumber = (receiptRequestBody.receiptPaymentInformation?.cardnumber.isNullOrEmpty()).let {
            if (it) null else receiptRequestBody.receiptPaymentInformation?.cardnumber
        }
        val accountnumber = (receiptRequestBody.receiptPaymentInformation?.accountnumber.isNullOrEmpty()).let {
            if (it) null else receiptRequestBody.receiptPaymentInformation?.accountnumber
        }

        if (cardnumber.isNullOrEmpty() && accountnumber.isNullOrEmpty()) {
            throw Exception("Card number or account number must be provided")
        }
        if (cardnumber != null && accountnumber != null) {
            throw Exception("Card and account number can not both be provided")
        }

        val committee = committeeService.getCommitteeById(receiptinfo.committee_id ?: throw Exception("Committee ID not provided"))
            ?: throw Exception("Committee not found")



        val receipt: Receipt = Receipt(
            "",
            receiptinfo.amount ?: throw Exception("Amount not provided"),
            committee,
            receiptinfo.name ?: throw Exception("Receipt name not provided"),
            receiptinfo.description ?: throw Exception("Receipt description not provided"),
            user,
            emptySet(),
            emptySet(),
            null,
            card_number = cardnumber,
            account_number = accountnumber

        )


        val storedReceipt = receiptRepository.save(receipt);

        /**
         * Save attachments
         */

        val attachments = receiptRequestBody.attachments
        val attachmentsnames = mutableListOf<String>()
        try {
            attachments.forEach { attachment ->
                val imgname = blobService.uploadFile(attachment)
                attachmentService.createAttachment(Attachment("", storedReceipt, imgname))
                println("Attachent name: $imgname")
                attachmentsnames.add(imgname)
            }
        } catch (e: Exception) {
            receiptRepository.delete(storedReceipt)
            throw e;
        }


        val emailContent = """
            <h2>Detaljer for innsendt kvittering</h2>
            <p><strong>Bruker:</strong> ${user.fullname}</p>
            <p><strong>Brukerens e-post:</strong> ${user.email}</p>
            <p><strong>Kvitterings-ID:</strong> ${storedReceipt.id}</p>
            <p><strong>Beløp:</strong> ${storedReceipt.amount}</p>
            <p><strong>Komité-ID:</strong> ${storedReceipt.committee}</p>
            <p><strong>Anledning:</strong> ${storedReceipt.name}</p>
            <p><strong>Beskrivelse:</strong> ${storedReceipt.description}</p>
            <p><strong>Betalingsmetode:</strong> ${
                if (receiptRequestBody.receiptPaymentInformation?.usedOnlineCard == true)
                    "Online-kort"
                else
                    "Bankoverføring"
                }</p>
            <p><strong>Kontonummer:</strong> ${
                receiptRequestBody.receiptPaymentInformation?.accountnumber ?: "Ikke oppgitt"
            }</p>
        """.trimIndent()

        mailService.sendEmail(user.email, "Receipt Submission Details", emailContent)
        return  ReceiptResponseBody()

    }

    fun getAllReceiptsFromUser(from: Int, count: Int, status: String?, committeeName: String?, search: String?, sortField: String?, sortOrder: String?): ReceiptListResponseBody? {


        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")


        val sort = if (!sortField.isNullOrEmpty()) {
            Sort.by(Sort.Direction.fromString(sortOrder ?: "ASC"), sortField)
        } else {
            Sort.by(Sort.Direction.DESC, "receiptCreatedAt")
        }
        val pageable = PageRequest.of(from, count, sort)

        val specification = ReceiptInfoViewSpecification(user.id, status, committeeName, search)

        val receiptPage = receiptInfoRepository.findAll(specification, pageable)
        val total: Long = receiptPage.totalElements

        val receiptResponseList = receiptPage.toList().map { receipt ->
            ReceiptInfoResponseBody(
                receiptId = receipt.receiptId,
                amount = receipt.amount.toString(),
                receiptName = receipt.receiptName,
                receiptDescription = receipt.receiptDescription,
                receiptCreatedAt = receipt.receiptCreatedAt.toString(),
                committeeName = receipt.committeeName,
                userFullname = receipt.userFullname,
                paymentOrCard = if (receipt.accountNumber != null) "Payment" else "Card",
                attachmentCount = receipt.attachmentCount.toInt(),
                latestReviewStatus = receipt.latestReviewStatus.toString(),
                latestReviewCreatedAt = receipt.latestReviewCreatedAt.toString(),
                latestReviewComment = receipt.latestReviewComment,
                paymentAccountNumber = receipt.accountNumber,
                cardCardNumber = receipt.cardNumber,
                attachments = listOf()
            )
        }
        return ReceiptListResponseBody(receiptResponseList.toTypedArray(), total)
    }

    fun getReceipt(id: String): CompleteReceipt? {
        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")
        val receipt = receiptInfoRepository.findById(id)
        if (receipt == null || receipt.userId != user.id) {
            return null
        }

        return getCompleteReceipt(receipt)





    }

    fun getCompleteReceipt(receipt: ReceiptInfo): CompleteReceipt {


        // Get files
        val attachments = attachmentService.getAttachmentsByReceiptId(receipt.receiptId)
        val files = attachments.map { attachment -> attachment.name.split(".")[1]+"."+blobService.downloadImage(attachment.name) }

        return CompleteReceipt(
            receipt.receiptId,
            receipt.amount,
            receipt.receiptName,
            receipt.receiptDescription,
            receipt.receiptCreatedAt,
            receipt.committeeName,
            receipt.userFullname,
            receipt.cardNumber?.let { "Card" } ?: "Payment",
            receipt.attachmentCount.toInt(),
            receipt.latestReviewStatus.toString(),
            receipt.latestReviewCreatedAt,
            receipt.latestReviewComment,
            receipt.accountNumber ?: "",
            receipt.cardNumber ?: "",
            files
        )


    }


}