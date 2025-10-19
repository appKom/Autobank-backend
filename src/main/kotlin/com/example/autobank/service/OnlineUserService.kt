package com.example.autobank.service

import com.example.autobank.data.authentication.Auth0User
import com.example.autobank.data.authentication.AuthenticatedUserResponse
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.repository.user.OnlineUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime


@Service
class OnlineUserService(
) {

    @Autowired
    lateinit var authenticationService: AuthenticationService

    @Autowired
    lateinit var onlineUserRepository: OnlineUserRepository




    fun getOnlineUser(): OnlineUser? {
        val sub: String = authenticationService.getUserSub()
        return onlineUserRepository.findByOnlineId(sub)
    }

    fun checkUser(): AuthenticatedUserResponse {
        val storedUser = onlineUserRepository.findByOnlineId(authenticationService.getUserSub())
        if (storedUser == null) {
            try {
                createOnlineUser()
            } catch (e: Exception) {
                return AuthenticatedUserResponse(success = false, false, false, null, "")
            }
        }

        return AuthenticatedUserResponse(success = true, authenticationService.checkAdmin(), false, expiresat = authenticationService.getExpiresAt(), fullname = authenticationService.getFullName())
    }

    fun createOnlineUser(): OnlineUser {
            val userinfo: Auth0User = authenticationService.getUserDetails()
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
