package com.example.autobank.repository

import com.example.autobank.data.models.EconomicrequestInfo
import com.example.autobank.data.models.Economicrequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification

interface EconomicrequestInfoRepository {
    fun findById(id: String): EconomicrequestInfo?
    fun findAll(spec: Specification<Economicrequest>, pageable: Pageable): Page<EconomicrequestInfo>
}
