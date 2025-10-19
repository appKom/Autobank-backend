package com.example.autobank.data.user

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "onlineuser")
open class OnlineUser(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @NotNull
    open val id: String,

    @Column(name = "email")
    @NotNull
    open val email: String,

    @Column(name = "fullname")
    open val fullname: String,

    @Column(name = "onlineid", unique = true)
    @NotNull
    open val onlineId: String,
    @Column(name = "isadmin")
    open var isAdmin: Boolean = false,
    @Column(name = "lastupdated")
    open var lastUpdated: LocalDateTime

    )