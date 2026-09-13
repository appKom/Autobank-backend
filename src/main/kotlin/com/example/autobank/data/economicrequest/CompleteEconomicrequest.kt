package com.example.autobank.data.economicrequest

import java.time.LocalDateTime

data class CompleteEconomicrequest(
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
    val committeeName: String?,
    val attachmentCount: Int,
    val latestReviewStatus: String?,
    val latestReviewCreatedAt: LocalDateTime?,
    val latestReviewComment: String?,
    val attachments: List<String>,
)
