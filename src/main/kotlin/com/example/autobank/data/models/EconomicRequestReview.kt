package com.example.autobank.data.models



import com.example.autobank.data.user.OnlineUser
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.jetbrains.annotations.NotNull
import java.time.LocalDateTime

enum class EconomicRequestStatus {
    APPROVED, DENIED
}

@Entity
@Table(name = "economicrequestreview")
class EconomicRequestReview (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    @NotNull
    val id: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "economicrequest_id", nullable = false)
    val economicrequest: Economicrequest,


    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @NotNull
    val status: EconomicRequestStatus,

    @Column(name = "comment")
    val comment: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onlineuser_id")
    @NotNull
    val user: OnlineUser,

    @CreationTimestamp
    @Column(name = "createdat")
    @NotNull
    val createdat: LocalDateTime?,
)
