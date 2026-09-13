package com.example.autobank.service

import com.example.autobank.data.economicrequest.EconomicrequestReviewRequestBody
import com.example.autobank.data.economicrequest.EconomicrequestReviewResponseBody
import com.example.autobank.data.models.EconomicRequestReview
import com.example.autobank.data.models.EconomicRequestStatus
import com.example.autobank.repository.EconomicRequestReviewRepository
import com.example.autobank.repository.EconomicrequestRepository
import org.springframework.stereotype.Service

@Service
class EconomicRequestReviewService(
    private val economicRequestReviewRepository: EconomicRequestReviewRepository,
    private val onlineUserService: OnlineUserService,
    private val economicrequestRepository: EconomicrequestRepository,
) {

    fun createEconomicrequestReview(reviewBody: EconomicrequestReviewRequestBody): EconomicrequestReviewResponseBody {
        val onlineuser = onlineUserService.getOnlineUser() ?: throw Exception("User not found")

        if (reviewBody.status != "APPROVED" && reviewBody.status != "DENIED") {
            throw Exception("Invalid status")
        }

        val economicrequest = economicrequestRepository.findById(reviewBody.economicrequestId)
            .orElseThrow { Exception("Economic request not found") }

        val prevReview = economicRequestReviewRepository.findFirstByEconomicrequestId(reviewBody.economicrequestId)
        if (prevReview != null) {
            economicRequestReviewRepository.deleteByEconomicrequestId(reviewBody.economicrequestId)
        }

        val savedReview = economicRequestReviewRepository.save(
            EconomicRequestReview(
                "",
                economicrequest,
                enumValueOf<EconomicRequestStatus>(reviewBody.status),
                reviewBody.comment,
                onlineuser,
                null,
            )
        )

        return EconomicrequestReviewResponseBody(
            id = savedReview.id,
            economicrequestId = savedReview.economicrequest.id,
            status = savedReview.status,
            comment = savedReview.comment,
            onlineUserId = savedReview.user.id,
            createdAt = savedReview.createdat,
        )
    }
}