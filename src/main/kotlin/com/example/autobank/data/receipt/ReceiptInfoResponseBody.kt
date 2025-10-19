package com.example.autobank.data.receipt

data class ReceiptInfoResponseBody(
    val receiptId: String,
    val amount: String,
    val receiptName: String,
    val receiptDescription: String,
    val receiptCreatedAt: String,
    val committeeName: String,
    val userFullname: String,
    val paymentOrCard: String,
    val attachmentCount: Int,
    val latestReviewStatus: String?,
    val latestReviewCreatedAt: String?,
    val latestReviewComment: String?,
    val paymentAccountNumber: String?,
    val cardCardNumber: String?,
    val attachments: List<String>
)