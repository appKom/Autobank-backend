package com.example.autobank.data.receipt

import java.math.BigDecimal
import java.time.LocalDateTime

data class CompleteReceipt(
    val receiptId: String,

    val amount: Double,

    val receiptName: String,

    val receiptDescription: String,

    val receiptCreatedAt: LocalDateTime,

    val committeeName: String,

    val userFullname: String,

    val paymentOrCard: String,

    val attachmentCount: Int,

    val latestReviewStatus: String?,

    val latestReviewCreatedAt: LocalDateTime?,

    val latestReviewComment: String?,

    val paymentAccountNumber: String,


    val cardUsed: String,

    val attachments: List<String>,

    val latestReviewerName: String? = null



)
