package com.example.autobank.data.economicrequest

import com.example.autobank.data.models.EconomicRequestStatus
import java.time.LocalDateTime

data class EconomicrequestReviewResponseBody(
    val id: String,
    val economicrequestId: String,
    val status: EconomicRequestStatus,
    val comment: String,
    val onlineUserId: String,
    val createdAt: LocalDateTime? = null,
)
