package com.example.autobank.service

import com.example.autobank.data.models.*
import com.example.autobank.data.receipt.*
import com.example.autobank.data.receipt.ReceiptInfoResponseBody
import com.example.autobank.repository.receipt.*
import com.example.autobank.repository.receipt.specification.ReceiptInfoViewSpecification
import org.springframework.stereotype.Service
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort

@Service
class ReceiptService(
    private val onlineUserService: OnlineUserService,
    private val receiptRepository: ReceiptRepository,
    private val blobService: BlobService,
    private val attachmentService: AttachmentService,
    private val committeeService: CommitteeService,
    private val receiptInfoRepository: ReceiptInfoRepositoryImpl
) {


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