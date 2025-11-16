package com.example.autobank.service

import com.example.autobank.data.authentication.Auth0User
import com.example.autobank.repository.user.OnlineUserRepository
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime


@Service
class AuthenticationService(
    @Value("\${admincommittee}") private val adminCommittee: String,
    @Value("\${auth0.domain}") private val domain: String,
    @Value("\${environment}") private val environment: String,
    @Value("\${api.base.domain}") private val apiBaseDomain: String,
) {

    private val restTemplate = RestTemplate()

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
            "$apiBaseDomain/user.getMe",
            HttpMethod.GET,
            entity,
        )

        // Navigate to the nested json object
        val result = response.body?.get("result") as? Map<*, *>
        val data = result?.get("data") as? Map<*, *>
        val json = data?.get("json") as? Map<*, *>

        return Auth0User(
            sub = json?.get("id")?.toString() ?: "",
            email = json?.get("email")?.toString() ?: "",
            name = json?.get("name")?.toString() ?: ""
        )
    }

    private fun fetchOnlineuserId(): Int {

        val input = mapOf("accessToken" to getAccessToken())
        val inputJson = ObjectMapper().writeValueAsString(input)
        val encodedInput = URLEncoder.encode(inputJson, StandardCharsets.UTF_8.toString())

        // Include the procedure name in the URL
        val endpoint = "${domain}/userinfo"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${getAccessToken()}")
        }
        val entity = HttpEntity<Void>(headers)

        val response: ResponseEntity<Map<String, Any>> = restTemplate.exchange(
            endpoint,
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
            return listOf("Applikasjonskomiteen")
        }

        // fetch users id, should probably be stored in user object
        val userId = fetchOnlineuserId()
        val input = mapOf("id" to userId)
        val inputJson = ObjectMapper().writeValueAsString(input)
        val encodedInput = URLEncoder.encode(inputJson, StandardCharsets.UTF_8.toString())
        val endpoint = "${apiBaseDomain}group.allByMember?input=$encodedInput"

        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer ${getAccessToken()}")
        }
        val entity = HttpEntity<Void>(headers)
        val response: ResponseEntity<UserCommitteeResponse> = restTemplate.exchange(
            endpoint,
            HttpMethod.GET,
            entity,
            object : ParameterizedTypeReference<UserCommitteeResponse>() {})

        if (response.statusCode.isError || response.body == null) {
            throw Exception("Error fetching user committees")
        }

        return response.body?.result?.data?.json?.map { it.slug } ?: listOf()
    }

    fun checkAdmin(): Boolean {

        val user = onlineUserRepository.findByOnlineId(getUserSub()) ?: throw Exception("User not found");

        // Time check for users last update isAdmin
        if (Duration.between(user.lastUpdated, LocalDateTime.now()).toMillis() > adminRecheckTime) {
            user.lastUpdated = LocalDateTime.now()

            // Check if the user is admin and set bool accordingly
            user.isAdmin = fetchUserCommittees().contains(adminCommittee)

            // I dont know what this is
            onlineUserRepository.save(user)
        }

        if (user.isAdmin) {
            return true;
        }

        return false;
    }

    fun getExpiresAt(): Instant? {
            val authentication = SecurityContextHolder.getContext().authentication
            return if (authentication is JwtAuthenticationToken) {
                val token = authentication.token
                token.getClaim("exp")
            } else {
                null
            }
        }

        data class UserCommitteeResponse(
            val result: Result
        )

        data class Result(
            val data: Data
        )

        data class Data(
            val json: List<Committee>
        )

        data class Committee(
            val slug: String,
            val abbreviation: String,
            val name: String,
            val type: String,
            val memberVisibility: String,
            // Add other fields if needed
        )

}
