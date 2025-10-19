package com.example.autobank.data.models

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import io.swagger.v3.oas.annotations.media.Schema

@Entity
@Table(name = "committee")
@Schema(description = "Committee information")
class Committee (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String,

    @Column(name = "name")
    @NotNull
    val name: String
)