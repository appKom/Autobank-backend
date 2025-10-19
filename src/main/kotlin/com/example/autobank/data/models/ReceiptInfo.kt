package com.example.autobank.data.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime

data class ReceiptInfo(
    val receiptId: String,
    val amount: Double,
    val receiptName: String,
    val receiptDescription: String,
    val receiptCreatedAt: LocalDateTime,
    val committeeName: String,
    val userFullname: String,
    val accountNumber: String?,
    val cardNumber: String?,
    val userId: String,
    val attachmentCount: Long,
    val latestReviewStatus: ReceiptStatus?,
    val latestReviewCreatedAt: LocalDateTime?,
    val latestReviewComment: String?,
)
