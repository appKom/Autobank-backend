/*package com.example.autobank.repository.receipt

import com.example.autobank.data.models.ReceiptInfo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository


interface ReceiptInfoViewRepository : JpaRepository<ReceiptInfo, Int>, JpaSpecificationExecutor<ReceiptInfo> {

    fun findByReceiptId(id: Int): ReceiptInfo

    fun findByUserId(pageable: Pageable, id: Int): Page<ReceiptInfo>

}
*/