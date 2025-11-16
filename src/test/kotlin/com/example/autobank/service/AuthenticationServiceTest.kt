package com.example.autobank.service

import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.user.OnlineUserRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.LocalDateTime

class AuthenticationServiceTest : FunSpec({

    val onlineUserRepository = mockk<OnlineUserRepository>()
    val authenticationService = AuthenticationService(
        adminCommittee = "Test Committee",
        domain = "test.auth0.com",
        environment = "test"
    ).apply {
        this.onlineUserRepository = onlineUserRepository
    }

    context("getUserSub") {
        test("should return empty string when no authentication context") {
            // When
            val result = authenticationService.getUserSub()

            // Then
            result shouldBe ""
        }
    }

    context("getFullName") {
        test("should return empty string") {
            // When
            val result = authenticationService.getFullName()

            // Then
            result shouldBe ""
        }
    }

    context("getAccessToken") {
        test("should return empty string when no authentication context") {
            // When
            val result = authenticationService.getAccessToken()

            // Then
            result shouldBe ""
        }
    }

    context("checkAdmin") {
        test("should return true for admin user") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", true, LocalDateTime.now())
            every { onlineUserRepository.findByOnlineId(any()) } returns mockUser

            // When
            val result = authenticationService.checkAdmin()

            // Then
            result shouldBe true
            verify { onlineUserRepository.findByOnlineId(any()) }
        }
        test("should throw Exception when user not found") {
            // Given
            every { onlineUserRepository.findByOnlineId(any()) } returns null

            // When & Then
            val exception = assertThrows<Exception> { authenticationService.checkAdmin() }
            exception.message shouldBe "User not found"
        }
    }

    context("getExpiresAt") {
        test("should return null when no authentication context") {
            // When
            val result = authenticationService.getExpiresAt()

            // Then
            result shouldBe null
        }
    }

    context("fetchUserCommittees") {
        test("should return test committees in dev environment") {
            // Given
            val testService = AuthenticationService(
                adminCommittee = "Test Committee",
                domain = "test.auth0.com",
                environment = "dev" // Non-prod environment
            )

            // When
            val result = testService.fetchUserCommittees()

            // Then
            result shouldBe listOf("Applikasjonskomiteen", "Trivselskomiteen")
        }
    }

    context("getSecondsUntilExpiration") {
        test("should return 0 when no expiration time") {
            // When
            val result = authenticationService.getSecondsUntilExpiration()

            // Then
            result shouldBe 0
        }
    }
})
