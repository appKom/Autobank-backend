package com.example.autobank.data.authentication

import com.nimbusds.openid.connect.sdk.claims.Gender
import org.jetbrains.annotations.NotNull
import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Auth0 user information")
class Auth0User(
    @NotNull
    val sub: String,
    @NotNull
    val email: String,
    @NotNull
    val name: String,
)