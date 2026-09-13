package com.example.autobank.data.models

import java.time.LocalDateTime

data class EconomicrequestInfo(
    val economicrequestId: String,
    val subject: String,
    val purpose: String,
    val date: LocalDateTime,
    val description: String,
    val amount: Double,
    val paymentDescription: String,
    val otherInformation: String?,
    val onlinemail: String?,
    val economicrequestCreatedAt: LocalDateTime,
    val userFullname: String,
    val userId: String,
    val committeeName: String?,
    val attachmentCount: Long,
    val latestReviewStatus: EconomicRequestStatus?,
    val latestReviewCreatedAt: LocalDateTime?,
    val latestReviewComment: String?,
)
