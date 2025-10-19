/*package com.example.autobank.service

import com.example.autobank.data.models.EconomicRequestReview
import com.example.autobank.data.models.Economicrequest
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.EconomicRequestReviewRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EconomicRequestReviewService {
    @Autowired
    lateinit var economicRequestReviewRepository: EconomicRequestReviewRepository
    fun checkIfRequest(economicrequest: Economicrequest): Boolean {
        return economicRequestReviewRepository.existsByEconomicRequestId(economicrequest.id)
    }

    fun createEconomicrequestReview(
        status: Boolean,
        user: OnlineUser,
        description: String,
        economicrequest: Economicrequest
    ) {
       /* val dato = LocalDateTime.now()
        val economicRequestReview = EconomicRequestReview(
            id = -1,
            economicrequestId = economicrequest.id,
            createdat = dato,
            onlineUserId = user.onlineId,
            comment = description,
            status = status
        )
        economicRequestReviewRepository.save(economicRequestReview)
*/
    }

    fun updateStatus(economicRequestReview: EconomicRequestReview, status: Boolean) {
        economicRequestReviewRepository.findEconomicRequestReviewsEconomicRequestIdAndUpdateStatus(
            economicRequestReview.id,
            status
        )
    }

    fun getReview(economicrequest: Economicrequest): EconomicRequestReview {
        return economicRequestReviewRepository.getEconomicRequestReviewByEconomicRequestId(economicrequest.id)

    }
}*/