package com.example.autobank.repository

import com.example.autobank.data.models.Economicrequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EconomicrequestRepository : JpaRepository<Economicrequest, String> {
    fun findAllByUserId(onlineUserId: String): List<Economicrequest>
}
