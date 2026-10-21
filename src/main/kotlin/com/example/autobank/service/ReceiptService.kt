package com.example.autobank.service

import com.example.autobank.data.models.*
import com.example.autobank.data.receipt.*
import com.example.autobank.data.receipt.ReceiptInfoResponseBody
import com.example.autobank.repository.receipt.*
import com.example.autobank.repository.receipt.specification.ReceiptInfoViewSpecification
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import com.example.autobank.service.MailService
import com.example.autobank.service.maxTotalFilesSize
import com.example.autobank.service.maxDescriptionLength

const val maxDescriptionLength = 500

@Service
class ReceiptService(
    private val onlineUserService: OnlineUserService,
    private val receiptRepository: ReceiptRepository,
    private val blobService: BlobService,
    private val attachmentService: AttachmentService,
    private val committeeService: CommitteeService,
    private val receiptInfoRepository: ReceiptInfoRepositoryImpl,
    private val mailService: MailService,
    private val coverPageService: CoverPageService,
    @Value("\${environment}") private val environment: String
) {

    fun createReceipt(receiptRequestBody: ReceiptRequestBody): ReceiptResponseBody {
        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")
        val receiptinfo = receiptRequestBody.receipt ?: throw Exception("Receipt not sent")


        val cardUsed = (receiptRequestBody.receiptPaymentInformation?.cardUsed.isNullOrEmpty()).let {
            if (it) null else receiptRequestBody.receiptPaymentInformation?.cardUsed
        }
        val accountnumber = (receiptRequestBody.receiptPaymentInformation?.accountnumber.isNullOrEmpty()).let {
            if (it) null else receiptRequestBody.receiptPaymentInformation?.accountnumber
        }

        if (cardUsed.isNullOrEmpty() && accountnumber.isNullOrEmpty()) {
            throw Exception("Card number or account number must be provided")
        }
        if (cardUsed != null && accountnumber != null) {
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
            card_number = cardUsed,
            account_number = accountnumber

        )
            val attachments = receiptRequestBody.attachments
            var totalFilesBytes = 0L
            attachments.forEach { attachment -> 
                totalFilesBytes += attachment.substringAfter("base64,").length.toLong() * 3 / 4
            }
            if (totalFilesBytes > maxTotalFilesSize){
                throw Exception("Total attachment size must be below $maxTotalFilesSize")
            }
            if (receipt.description.length > maxDescriptionLength){
                throw Exception("Receipt description must have $maxDescriptionLength or fewer characters")
            }
            val storedReceipt = receiptRepository.save(receipt)

            /**
             * Save attachments and prepare for email
             */
            val attachmentsForEmail = mutableListOf<Pair<String, ByteArray>>()

            try {
                attachments.forEach { file64 ->
                    // 1. Upload to storage (this returns the generated filename with UUID)
                    val imgname = blobService.uploadFile(file64)
                    attachmentService.createAttachment(Attachment("", storedReceipt, imgname))

                    val pureBase64 = if (file64.contains(",")) file64.split(",")[1] else file64
                    val bytearray = java.util.Base64.getDecoder().decode(pureBase64)

                    attachmentsForEmail.add(imgname to bytearray)
                }
            } catch (e: Exception) {
                receiptRepository.delete(storedReceipt)

                throw e
            }           

            val emailContent = """
                <h2>Detaljer for innsendt kvittering</h2>
                <p><strong>Bruker:</strong> ${user.fullname}</p>
                <p><strong>Brukerens e-post:</strong> ${user.email}</p>
                <p><strong>Kvitterings-ID:</strong> ${storedReceipt.id}</p>
                <p><strong>Beløp:</strong> ${storedReceipt.amount}</p>
                <p><strong>Komité-ID:</strong> ${storedReceipt.committee.name}</p>
                <p><strong>Anledning:</strong> ${storedReceipt.name}</p>
                <p><strong>Beskrivelse:</strong> ${storedReceipt.description}</p>
                <p><strong>Betalingsmetode:</strong> ${
                    if (receiptRequestBody.receiptPaymentInformation?.usedOnlineCard == true) "Online-kort" else "Bankoverføring"
                }</p>
                ${if (receiptRequestBody.receiptPaymentInformation?.usedOnlineCard != true) 
                    "<p><strong>Kontonummer:</strong> ${receiptRequestBody.receiptPaymentInformation?.accountnumber ?: "Ikke oppgitt"}</p>" 
                else ""}
            """.trimIndent()

                val coverPageData = CoverPageService.CoverPageData(
                    name = user.fullname,
                    email = user.email,
                    committeeName = storedReceipt.committee.name,
                    date = java.time.LocalDate.now().toString(),
                    accountNumber = receiptRequestBody.receiptPaymentInformation?.accountnumber,
                    amount = storedReceipt.amount.toString(),
                    occasion = storedReceipt.name,
                    type = if (receiptRequestBody.receiptPaymentInformation?.usedOnlineCard == true) "Online-kort" else "Utlegg",
                    comment = storedReceipt.description
                )
                val combinedPdf = coverPageService.generateCombinedPdf(coverPageData, attachmentsForEmail)

                val fikenAttachments = listOf("kvitteringsskjema.pdf" to combinedPdf)

            // 3. Send email with the collected attachments
            mailService.sendEmail(
                toEmail = user.email,
                subject = "Receipt Submission Details",
                htmlBody = emailContent,
                attachments = fikenAttachments,

            )

            if (environment == "prod") {


                mailService.sendEmail(
                    toEmail = "online-linjeforeningen-for-informatikk1@bilag.fiken.no",

                    subject = "Kvittering: ${storedReceipt.committee.name} - ${user.fullname} - ${storedReceipt.name}",
                    attachments = fikenAttachments,

                    htmlBody = emailContent
                )
                println("Email sent to Fiken")
            }

            return ReceiptResponseBody()
        }


    fun getAllReceiptsFromUser(from: Int, count: Int, status: String?, committeeName: String?, search: String?, sortField: String?, sortOrder: String?): ReceiptListResponseBody? {


        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")


        val sort = if (!sortField.isNullOrEmpty()) {
            Sort.by(Sort.Direction.fromString(sortOrder ?: "ASC"), sortField)
        } else {
            Sort.by(Sort.Direction.DESC, "createdat")
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
                cardUsed = receipt.cardUsed,
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
        val files = attachments.map { attachment -> attachment.name.split(".")[1].replace(":", "/")+"."+blobService.downloadImage(attachment.name) }

        return CompleteReceipt(
            receipt.receiptId,
            receipt.amount,
            receipt.receiptName,
            receipt.receiptDescription,
            receipt.receiptCreatedAt,
            receipt.committeeName,
            receipt.userFullname,
            receipt.cardUsed?.let { "Card" } ?: "Payment",
            receipt.attachmentCount.toInt(),
            receipt.latestReviewStatus.toString(),
            receipt.latestReviewCreatedAt,
            receipt.latestReviewComment,
            receipt.accountNumber ?: "",
            receipt.cardUsed ?: "",
            files
        )


    }


}
