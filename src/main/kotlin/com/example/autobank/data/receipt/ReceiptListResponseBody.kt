package com.example.autobank.data.receipt

import com.example.autobank.data.receipt.ReceiptInfoResponseBody

class ReceiptListResponseBody(
    val receipts: Array<ReceiptInfoResponseBody>,
    val total: Long,
)