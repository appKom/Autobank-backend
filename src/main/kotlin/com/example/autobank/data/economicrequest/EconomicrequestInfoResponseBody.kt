package com.example.autobank.data.economicrequest

data class EconomicrequestInfoResponseBody(
    val economicrequestId: String,
    val subject: String,
    val purpose: String,
    val description: String,
    val amount: String,
    val economicrequestCreatedAt: String,
    val userFullname: String,
    val committeeName: String?,
    val onlinemail: String?,
    val attachmentCount: Int,
    val latestReviewStatus: String?,
    val latestReviewCreatedAt: String?,
    val latestReviewComment: String?,
)
