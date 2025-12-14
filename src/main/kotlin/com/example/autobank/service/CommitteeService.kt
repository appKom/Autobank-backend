package com.example.autobank.service;

import com.example.autobank.data.models.Committee
import com.example.autobank.data.user.UserCommitteeResponseBody
import com.example.autobank.repository.CommitteeRepository
import org.springframework.stereotype.Service

@Service
class CommitteeService(
    private val committeeRepository: CommitteeRepository,
    private val authenticationService: AuthenticationService
) {

    fun getAllCommittees(): List<Committee> {
        return committeeRepository.findAll()
    }

    fun getUserAndCommittees(): UserCommitteeResponseBody {
        val userdetails = authenticationService.getUserDetails()

        return UserCommitteeResponseBody(userdetails.name, userdetails.email, authenticationService.fetchUserCommittees())
    }

    fun getCommitteeById(committeeId: String): Committee? {
        return committeeRepository.findById(committeeId).orElse(null)

    }
}