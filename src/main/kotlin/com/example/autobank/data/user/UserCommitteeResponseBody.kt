package com.example.autobank.data.user;

data class UserCommitteeResponseBody (
    val name: String,
    val email: String,
    val committees: List<String>,
    val imageUrl: String? 
)