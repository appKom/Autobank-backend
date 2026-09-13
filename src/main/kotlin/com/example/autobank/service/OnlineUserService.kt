package com.example.autobank.service

import com.example.autobank.data.authentication.Auth0User
import com.example.autobank.data.authentication.AuthenticatedUserResponse
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.user.OnlineUserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class OnlineUserService(
    private val authenticationService: AuthenticationService,
    private val onlineUserRepository: OnlineUserRepository
) {

    private val log = LoggerFactory.getLogger(OnlineUserService::class.java)

    fun getOnlineUser(): OnlineUser? {
        val sub: String = authenticationService.getUserSub()
        log.debug("getOnlineUser: resolved sub='$sub'")

        if (sub.isBlank()) {
            log.warn("getOnlineUser: sub is blank — JWT may be missing or invalid")
            return null
        }

        val user = onlineUserRepository.findByOnlineId(sub)
        if (user == null) {
            log.warn("getOnlineUser: no local user for sub='$sub', auto-creating")
            return try {
                val created = createOnlineUser()
                log.info("getOnlineUser: auto-created user id=${created.id} for sub='$sub'")
                created
            } catch (e: Exception) {
                log.error("getOnlineUser: failed to auto-create user for sub='$sub': ${e.message}", e)
                null
            }
        }
        log.debug("getOnlineUser: found user id=${user.id} for sub='$sub'")
        return user
    }

    fun checkUser(): AuthenticatedUserResponse {
        val sub = authenticationService.getUserSub()
        log.debug("checkUser: sub='$sub'")

        val storedUser = onlineUserRepository.findByOnlineId(sub)
        if (storedUser == null) {
            log.info("checkUser: no local user for sub='$sub', attempting to create")
            try {
                createOnlineUser()
                log.info("checkUser: created local user for sub='$sub'")
            } catch (e: Exception) {
                log.error("checkUser: failed to create user for sub='$sub': ${e.message}", e)
                return AuthenticatedUserResponse(success = false, false, false, null, "")
            }
        }

        return AuthenticatedUserResponse(success = true, authenticationService.checkAdmin(), false, expiresat = authenticationService.getExpiresAt(), fullname = authenticationService.getFullName())
    }

    fun createOnlineUser(): OnlineUser {
        log.debug("createOnlineUser: fetching user details from external API")
        val userinfo: Auth0User = authenticationService.getUserDetails()
        log.info("createOnlineUser: creating local user for sub='${userinfo.sub}' email='${userinfo.email}'")

        val onlineUser = OnlineUser(
            id = "",
            onlineId = userinfo.sub,
            email = userinfo.email,
            fullname = userinfo.name,
            isAdmin = false,
            lastUpdated = LocalDateTime.of(2000, 1, 1, 0, 0)
        )

        return onlineUserRepository.save(onlineUser)
    }

}
