package com.example.autobank.repository

import com.example.autobank.data.models.EconomicRequestAttachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EconomicRequestAttachmentRepository : JpaRepository<EconomicRequestAttachment, String> {
    fun findByEconomicrequestId(economicrequestId: String): List<EconomicRequestAttachment>
}
