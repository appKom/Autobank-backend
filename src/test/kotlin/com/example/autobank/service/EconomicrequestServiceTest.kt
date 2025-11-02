package com.example.autobank.service

import com.example.autobank.data.models.Economicrequest
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.EconomicrequestRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class EconomicrequestServiceTest : FunSpec({

    val economicrequestRepository = mockk<EconomicrequestRepository>()
    val onlineUserService = mockk<OnlineUserService>()
    val economicrequestService = EconomicrequestService(economicrequestRepository, onlineUserService)

    context("deleteEconomicrequest") {
        test("should delete economic request successfully") {
            // Given
            val requestId = 1
            every { economicrequestRepository.deleteById(requestId) } returns Unit

            // When
            economicrequestService.deleteEconomicrequest(requestId)

            // Then
            verify { economicrequestRepository.deleteById(requestId) }
        }
    }

    context("getEconomicrequest") {
        test("should return economic request when found") {
            // Given
            val requestId = 1
            val mockUser = OnlineUser("1", "test-sub", "Test User", "test@example.com", false, LocalDateTime.now())
            val economicrequest = Economicrequest(
                id = requestId.toString(),
                subject = "Test Subject",
                purpose = "Test Purpose",
                date = LocalDateTime.now(),
                duration = "1 hour",
                description = "Test Description",
                amount = BigDecimal.valueOf(5000.0),
                personCount = 1,
                names = "Test User",
                paymentDescription = "Test Payment",
                otherInformation = "Test Info",
                createdat = LocalDateTime.now(),
                user = mockUser
            )
            
            every { economicrequestRepository.findById(requestId) } returns Optional.of(economicrequest)

            // When
            val result = economicrequestService.getEconomicrequest(requestId)

            // Then
            result shouldNotBe null
            result shouldBe economicrequest
            result.id shouldBe requestId.toString()
            result.subject shouldBe "Test Subject"
            result.description shouldBe "Test Description"
            result.amount shouldBe BigDecimal.valueOf(5000.0)
            verify { economicrequestRepository.findById(requestId) }
        }


        test("should throw NoSuchElementException when request not found") {
            // Given
            val requestId = 999
            every { economicrequestRepository.findById(requestId) } returns Optional.empty()

            // When
            shouldThrow<NoSuchElementException> { economicrequestService.getEconomicrequest(requestId) }
        }
    }
})
