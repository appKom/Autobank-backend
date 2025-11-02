package com.example.autobank.service

import com.example.autobank.data.authentication.Auth0User
import com.example.autobank.repository.user.OnlineUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime


@Service
class AuthenticationService(
    @Value("\${admincommittee}") private val adminCommittee: String,
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${environment}") private val environment: String,
) {

    private val restTemplate = RestTemplate()

    private val fetchProfileUrl = "https://old.online.ntnu.no/api/v1/profile/"

    private val fetchUserCommitteesUrl = "https://old.online.ntnu.no/api/v1/group/online-groups/?members__user="

    private val adminRecheckTime = 24 * 60 * 60 * 1000;

    @Autowired
    lateinit var onlineUserRepository: OnlineUserRepository

    fun getSecondsUntilExpiration(): Long {
        val expiresAt = getExpiresAt()
        return if (expiresAt != null) {
            expiresAt.epochSecond - Instant.now().epochSecond
        } else {
            0
        }
    }

    fun getUserSub(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is JwtAuthenticationToken) {
            val token = authentication.token
            token.getClaim("sub")
        } else {
            ""
        }
    }

    fun getFullName(): String {
       return "";
    }

    fun getAccessToken(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is JwtAuthenticationToken) {
            val token = authentication.token
            token.tokenValue
        } else {
            ""
        }
    }

    fun getUserDetails(): Auth0User {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${getAccessToken()}")
        }
        val entity = HttpEntity<Void>(headers)
        val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
            "${domain}/userinfo",
            HttpMethod.GET,
            entity,
        )

        return Auth0User(
            sub = response.body?.get("sub").toString(),
            email = response.body?.get("email").toString(),
            name = response.body?.get("name").toString(),
        )
    }

    private fun fetchOnlineuserId(): Int {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${getAccessToken()}")
        }
        val entity = HttpEntity<Void>(headers)
        val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
            fetchProfileUrl,
            HttpMethod.GET,
            entity,
        )

        if (response.statusCode.isError || response.body == null) {
            throw Exception("Error fetching user id")
        }

        return response.body?.get("id").toString().toInt()
    }

    fun fetchUserCommittees(): List<String> {

        if (environment != "prod") {
            return listOf("Applikasjonskomiteen", "Trivselskomiteen")
        }

        val userId = fetchOnlineuserId()

        val headers = HttpHeaders()
        val entity = HttpEntity<Void>(headers)
        val response: ResponseEntity<UserCommitteeResponse> = restTemplate.exchange(
            fetchUserCommitteesUrl + userId,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<UserCommitteeResponse>() {}
        )

        if (response.statusCode.isError || response.body == null) {
            throw Exception("Error fetching user committees")
        }

        return response.body?.results?.map { it.name_long } ?: listOf()
    }

    fun checkAdmin(): Boolean {

        val user = onlineUserRepository.findByOnlineId(getUserSub()) ?: throw Exception("User not found");
        val currentTime = LocalDateTime.now()
        if (Duration.between(user.lastUpdated, currentTime).toMillis() > adminRecheckTime) {
            user.lastUpdated = currentTime

            val isAdmin = checkBankomMembership()
            user.isAdmin = isAdmin
            onlineUserRepository.save(user)

            return isAdmin;
        } else {
            return user.isAdmin;
        }
    }

    private fun checkBankomMembership(): Boolean {
        if (environment == "dev") {
            return true
        }

        val userCommittees = fetchUserCommittees()
        return userCommittees.contains(adminCommittee)
    }

    /*
    fun checkSuperAdmin(): Boolean {
        return superadminEmails.split(",").contains(getUserDetails().email)
    }
    */


    fun getExpiresAt(): Instant? {
            val authentication = SecurityContextHolder.getContext().authentication
            return if (authentication is JwtAuthenticationToken) {
                val token = authentication.token
                token.getClaim("exp")
            } else {
                null
            }
        }

    data class Result(
        val name_long: String = ""
    )

    data class UserCommitteeResponse(
        val results: List<Result> = listOf()
    )

}


