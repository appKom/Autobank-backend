package com.example.autobank.security

import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jwt.Jwt
import org.slf4j.LoggerFactory

class AudienceValidator(private val audience: String) : OAuth2TokenValidator<Jwt> {

    private val logger = LoggerFactory.getLogger(AudienceValidator::class.java)

    override fun validate(jwt: Jwt): OAuth2TokenValidatorResult {
        logger.info("=== Validating JWT Audience ===")
        logger.info("Expected audience: $audience")
        logger.info("Token audiences: ${jwt.audience}")

        val error = OAuth2Error("invalid_token", "The required audience is missing", null)
        return if (jwt.audience.contains(audience)) {
            logger.info("Audience validation: SUCCESS")
            OAuth2TokenValidatorResult.success()
        } else {
            logger.error("Audience validation: FAILED - Required audience '$audience' not found in ${jwt.audience}")
            OAuth2TokenValidatorResult.failure(error)
        }
    }
}