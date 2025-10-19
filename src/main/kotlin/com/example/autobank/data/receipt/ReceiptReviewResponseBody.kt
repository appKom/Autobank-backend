package com.example.autobank.data.receipt

import com.example.autobank.data.models.Receipt
import com.example.autobank.data.models.ReceiptStatus
import com.example.autobank.data.user.OnlineUser
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

data class ReceiptReviewResponseBody (
    val id: String,
    val receiptId: String,
    val status: ReceiptStatus,
    val comment: String,
    val onlineUserId: String,
    val createdAt: LocalDateTime? = null

)
