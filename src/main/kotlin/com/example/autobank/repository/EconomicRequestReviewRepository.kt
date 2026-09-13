package com.example.autobank.repository

import com.example.autobank.data.models.EconomicRequestReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface EconomicRequestReviewRepository : JpaRepository<EconomicRequestReview, String> {
    fun findFirstByEconomicrequestId(economicrequestId: String): EconomicRequestReview?

    @Modifying
    @Transactional
    fun deleteByEconomicrequestId(economicrequestId: String)
}
