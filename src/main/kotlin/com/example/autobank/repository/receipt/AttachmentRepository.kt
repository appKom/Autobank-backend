package com.example.autobank.repository.receipt

import com.example.autobank.data.models.Attachment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AttachmentRepository : JpaRepository<Attachment, Int> {
    fun findByReceiptId(receiptId: String): List<Attachment>
}