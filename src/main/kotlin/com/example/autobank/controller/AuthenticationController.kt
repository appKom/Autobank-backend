package com.example.autobank.controller

import com.example.autobank.data.authentication.AuthenticatedUserResponse
import com.example.autobank.service.OnlineUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Endpoints for user authentication")
class AuthenticationController {
    
    private val logger = LoggerFactory.getLogger(AuthenticationController::class.java)
    
    @Autowired
    lateinit var onlineUserService: OnlineUserService

    @Operation(summary = "Check authenticated user", description = "Returns information about the currently authenticated user")
    @GetMapping("/getuser")
    fun checkUser(): ResponseEntity<AuthenticatedUserResponse> {
        return try {
            logger.info("=== /api/auth/getuser called ===")
            val result = onlineUserService.checkUser()
            logger.info("checkUser result: success=${result.success}, isadmin=${result.isadmin}")
            ResponseEntity.ok().body(result)
        } catch (e: Exception) {
            logger.error("=== Exception in /api/auth/getuser ===", e)
            logger.error("Exception type: ${e.javaClass.name}")
            logger.error("Exception message: ${e.message}")
            logger.error("Stack trace:", e)
            ResponseEntity.badRequest().build()
        }
    }
}