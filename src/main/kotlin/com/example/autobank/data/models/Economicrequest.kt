package com.example.autobank.data.models

import com.example.autobank.data.user.OnlineUser
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime


@Entity
@Table(name = "economicrequest")
class Economicrequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @NotNull
    val id: String,

    @Column(name = "subject")
    val subject: String,

    @Column(name = "purpose")
    val purpose: String,

    @Column(name = "date")
    val date: LocalDateTime,

    @Column(name = "description")
    val description: String,

    @Column(name = "amount", nullable = false)
    val amount: BigDecimal,

    @Column(name = "paymentdescription")
    val paymentDescription: String,

    @Column(name = "otherinformation")
    val otherInformation: String?,

    @Column(name = "onlinemail")
    val onlinemail: String?,

    @CreationTimestamp
    @Column(name = "createdat")
    @NotNull
    val createdat: LocalDateTime?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id")
    val committee: Committee? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onlineuser_id")
    @NotNull
    val user: OnlineUser,

    @OneToMany(mappedBy = "economicrequest")
    val attachments: Set<EconomicRequestAttachment> = emptySet(),

    @OneToMany(mappedBy = "economicrequest")
    val reviews: Set<EconomicRequestReview> = emptySet(),

    )

