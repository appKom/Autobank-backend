package com.example.autobank.data.models


import com.example.autobank.data.user.OnlineUser
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "receipt")
class Receipt(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @NotNull
    val id: String,

    @Column(name = "amount")
    val amount: Double,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "committee_id")
    @NotNull
    val committee: Committee,

    @Column(name = "name")
    val name: String,

    @Column(name = "description")
    val description: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onlineuser_id")
    @NotNull
    val user: OnlineUser,

    @OneToMany(mappedBy = "receipt")
    val attachments: Set<Attachment> = emptySet(),

    @OneToMany(mappedBy = "receipt")
    val reviews: Set<ReceiptReview> = emptySet(),

    @CreationTimestamp
    @Column(name = "createdat")
    @NotNull
    val createdat: LocalDateTime?,

    @Column(name = "card_number")
    val card_number: String? = null,

    @Column(name = "account_number")
    val account_number: String? = null


)