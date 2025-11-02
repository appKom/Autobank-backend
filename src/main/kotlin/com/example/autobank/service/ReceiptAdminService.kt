package com.example.autobank.service

import com.example.autobank.data.models.ReceiptInfo
import com.example.autobank.data.models.ReceiptStatus
import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired

import com.example.autobank.repository.receipt.ReceiptInfoRepositoryImpl
import com.example.autobank.data.receipt.*
import com.example.autobank.repository.receipt.specification.ReceiptInfoViewSpecification
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort


@Service
class ReceiptAdminService(
    private val receiptInfoRepository: ReceiptInfoRepositoryImpl,
    private val receiptService: ReceiptService
) {

    fun getAll(from: Int, count: Int, status: String?, committeeName: String?, search: String?, sortField: String?, sortOrder: String?): ReceiptListResponseBody? {


        val sort = if (!sortField.isNullOrEmpty()) {
            Sort.by(Sort.Direction.fromString(sortOrder ?: "ASC"), sortField)
        } else {
            Sort.by(Sort.Direction.DESC, "receiptCreatedAt")
        }

        val pageable = PageRequest.of(from, count, sort)

        val specification = ReceiptInfoViewSpecification(null, status, committeeName, search)

        val receipts = receiptInfoRepository.findAll(specification, pageable);

        val receiptInfoResponseList = receipts.toList().map { receipt ->
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

        val total: Long = receipts.totalElements // receiptInfoRepository.count(specification)

        return ReceiptListResponseBody(receiptInfoResponseList.toTypedArray(), total)


    }

    fun getReceipt(id: String): CompleteReceipt? {
        val receipt = receiptInfoRepository.findById(id) ?: return null

        return receiptService.getCompleteReceipt(receipt)

    }


}