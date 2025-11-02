package com.example.autobank.service

import com.example.autobank.data.authentication.Auth0User
import com.example.autobank.data.models.Committee
import com.example.autobank.data.user.UserCommitteeResponseBody
import com.example.autobank.repository.CommitteeRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class CommitteeServiceTest : FunSpec({

    val committeeRepository = mockk<CommitteeRepository>()
    val authenticationService = mockk<AuthenticationService>()
    val committeeService = CommitteeService(committeeRepository, authenticationService)

    context("getAllCommittees") {
        test("should return all committees") {
            // Given
            val expectedCommittees = listOf(
                Committee("1", "Applikasjonskomiteen"),
                Committee("2", "Trivselskomiteen")
            )
            every { committeeRepository.findAll() } returns expectedCommittees

            // When
            val result = committeeService.getAllCommittees()

            // Then
            result shouldBe expectedCommittees
            verify { committeeRepository.findAll() }
        }

        test("should return empty list when no committees exist") {
            // Given
            every { committeeRepository.findAll() } returns emptyList()

            // When
            val result = committeeService.getAllCommittees()

            // Then
            result shouldBe emptyList()
        }
    }

    context("getUserAndCommittees") {
        test("should return user information and committee list") {
            // Given
            val mockUserDetails = Auth0User(
                sub = "test-sub",
                email = "test@example.com",
                name = "Test User"
            )
            val mockCommittees = listOf("Applikasjonskomiteen", "Trivselskomiteen")
            
            every { authenticationService.getUserDetails() } returns mockUserDetails
            every { authenticationService.fetchUserCommittees() } returns mockCommittees

            // When
            val result = committeeService.getUserAndCommittees()

            // Then
            result shouldBe UserCommitteeResponseBody(
                name = "Test User",
                email = "test@example.com",
                committees = mockCommittees
            )
            verify { authenticationService.getUserDetails() }
            verify { authenticationService.fetchUserCommittees() }
        }
    }

    context("getCommitteeById") {
        test("should find committee by existing ID") {
            // Given
            val committeeId = "1"
            val expectedCommittee = Committee("1", "Applikasjonskomiteen")
            every { committeeRepository.findById(committeeId) } returns Optional.of(expectedCommittee)

            // When
            val result = committeeService.getCommitteeById(committeeId)

            // Then
            result shouldBe expectedCommittee
            verify { committeeRepository.findById(committeeId) }
        }

        test("should return null for non-existing committee ID") {
            // Given
            val committeeId = "999"
            every { committeeRepository.findById(committeeId) } returns Optional.empty()

            // When
            val result = committeeService.getCommitteeById(committeeId)

            // Then
            result shouldBe null
            verify { committeeRepository.findById(committeeId) }
        }
    }
})
