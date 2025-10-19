package com.example.autobank.data.models

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import io.swagger.v3.oas.annotations.media.Schema

@Entity
@Table(name = "attachment")
@Schema(description = "Attachment information")
class Attachment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @NotNull
    @Schema(description = "Unique identifier for the attachment")
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    @NotNull
    val receipt: Receipt,

    @Column(name = "name")
    @NotNull
    val name: String,

    )