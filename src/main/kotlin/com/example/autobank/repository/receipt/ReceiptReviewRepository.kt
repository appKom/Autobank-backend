package com.example.autobank.repository.receipt

import com.example.autobank.data.models.ReceiptReview
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional

@Repository
interface ReceiptReviewRepository : JpaRepository<ReceiptReview, String> {
    fun findFirstByReceiptId(receiptId: String): ReceiptReview?

    @Query("SELECT r.user.fullname FROM ReceiptReview r WHERE r.receipt.id = :receiptId")
    fun findReviewerNameByReceiptId(receiptId: String): String?

    @Modifying
    @Transactional
    fun deleteByReceiptId(receiptId: String)

}
