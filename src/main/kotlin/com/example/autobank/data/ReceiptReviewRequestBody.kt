package com.example.autobank.data

import jakarta.persistence.Entity


data class ReceiptReviewRequestBody (
    val receiptId: String,
    val status: String,
    val comment: String,
)