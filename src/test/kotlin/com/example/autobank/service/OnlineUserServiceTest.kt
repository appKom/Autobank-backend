package com.example.autobank.service

import com.example.autobank.data.authentication.Auth0User
import com.example.autobank.data.authentication.AuthenticatedUserResponse
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.user.OnlineUserRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.time.LocalDateTime

class OnlineUserServiceTest : FunSpec({

    val authenticationService = mockk<AuthenticationService>()
    val onlineUserRepository = mockk<OnlineUserRepository>()
    
    val onlineUserService = OnlineUserService(authenticationService, onlineUserRepository)

    context("getOnlineUser") {
        test("should return user when found") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            every { authenticationService.getUserSub() } returns "test-sub"
            every { onlineUserRepository.findByOnlineId("test-sub") } returns mockUser

            // When
            val result = onlineUserService.getOnlineUser()

            // Then
            result shouldNotBe null
            result shouldBe mockUser
            verify { authenticationService.getUserSub() }
            verify { onlineUserRepository.findByOnlineId("test-sub") }
        }

        test("should return null when user not found") {
            // Given
            every { authenticationService.getUserSub() } returns "test-sub"
            every { onlineUserRepository.findByOnlineId("test-sub") } returns null

            // When
            val result = onlineUserService.getOnlineUser()

            // Then
            result shouldBe null
            verify { authenticationService.getUserSub() }
            verify { onlineUserRepository.findByOnlineId("test-sub") }
        }
    }

    context("checkUser") {
        test("should return success response when user exists") {
            // Given
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockExpiresAt = Instant.now().plusSeconds(3600)
            
            every { authenticationService.getUserSub() } returns "test-sub"
            every { onlineUserRepository.findByOnlineId("test-sub") } returns mockUser
            every { authenticationService.checkAdmin() } returns true
            every { authenticationService.getExpiresAt() } returns mockExpiresAt
            every { authenticationService.getFullName() } returns "Test User"

            // When
            val result = onlineUserService.checkUser()

            // Then
            result shouldNotBe null
            result.success shouldBe true
            result.isadmin shouldBe true
            result.expiresat shouldBe mockExpiresAt
            result.fullname shouldBe "Test User"
            verify { authenticationService.getUserSub() }
            verify { onlineUserRepository.findByOnlineId("test-sub") }
        }

        test("should create user and return success when user does not exist") {
            // Given
            val mockAuth0User = Auth0User("test-sub", "test@example.com", "Test User")
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val mockExpiresAt = Instant.now().plusSeconds(3600)
            
            every { authenticationService.getUserSub() } returns "test-sub"
            every { onlineUserRepository.findByOnlineId("test-sub") } returns null
            every { authenticationService.getUserDetails() } returns mockAuth0User
            every { onlineUserRepository.save(any()) } returns mockUser
            every { authenticationService.checkAdmin() } returns false
            every { authenticationService.getExpiresAt() } returns mockExpiresAt
            every { authenticationService.getFullName() } returns "Test User"

            // When
            val result = onlineUserService.checkUser()

            // Then
            result shouldNotBe null
            result.success shouldBe true
            result.isadmin shouldBe false
            verify { authenticationService.getUserDetails() }
            verify { onlineUserRepository.save(any()) }
        }

        test("should return failure response when user creation fails") {
            // Given
            every { authenticationService.getUserSub() } returns "test-sub"
            every { onlineUserRepository.findByOnlineId("test-sub") } returns null
            every { authenticationService.getUserDetails() } throws Exception("API Error")

            // When
            val result = onlineUserService.checkUser()

            // Then
            result shouldNotBe null
            result.success shouldBe false
            result.isadmin shouldBe false
            result.expiresat shouldBe null
            result.fullname shouldBe ""
        }
    }

    context("createOnlineUser") {
        test("should create and return online user") {
            // Given
            val mockAuth0User = Auth0User("test-sub", "test@example.com", "Test User")
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            
            every { authenticationService.getUserDetails() } returns mockAuth0User
            every { onlineUserRepository.save(any()) } returns mockUser

            // When
            val result = onlineUserService.createOnlineUser()

            // Then
            result shouldNotBe null
            result shouldBe mockUser
            verify { authenticationService.getUserDetails() }
            verify { onlineUserRepository.save(any()) }
        }
    }
})
