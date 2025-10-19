package com.example.autobank.repository.receipt

import com.example.autobank.data.models.ReceiptInfo
import com.example.autobank.data.models.Receipt
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification

interface ReceiptInfoRepository {
    fun findById(id: String): ReceiptInfo?

    fun findAll(spec: Specification<Receipt>, pageable: Pageable): Page<ReceiptInfo>

}