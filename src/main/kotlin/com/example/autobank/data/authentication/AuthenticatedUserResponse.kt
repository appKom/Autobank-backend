package com.example.autobank.data.authentication;

import java.time.Instant
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Authenticated user information")
class AuthenticatedUserResponse(
    val success: Boolean,
    val isadmin: Boolean,
    val issuperadmin: Boolean,
    val expiresat: Instant?,
    val fullname: String
)