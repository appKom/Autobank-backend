package com.example.autobank.data.models

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "economicrequestattachment")
class EconomicRequestAttachment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @NotNull
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "economicrequest_id", nullable = false)
    @NotNull
    val economicrequest: Economicrequest,

    @Column(name = "name")
    @NotNull
    val name: String,

    )