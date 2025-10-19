package com.example.autobank.repository.receipt.specification

import com.example.autobank.data.models.Receipt
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification
import com.example.autobank.data.models.Committee
import com.example.autobank.data.user.OnlineUser
import com.example.autobank.data.models.ReceiptReview

class ReceiptInfoViewSpecification(
    private val userId: String?,
    private val status: String?,
    private val committeeName: String?,
    private val search: String?
) : Specification<Receipt> {

    override fun toPredicate(
        root: Root<Receipt>,
        query: CriteriaQuery<*>,
        cb: CriteriaBuilder
    ): Predicate? {
        val predicates = mutableListOf<Predicate>()

        // Join to related entities needed for filtering
        val userJoin: Join<Receipt, OnlineUser> = root.join("user")
        val committeeJoin: Join<Receipt, Committee> = root.join("committee", JoinType.LEFT)
        val reviewsJoin: SetJoin<Receipt, ReceiptReview> = root.joinSet("reviews", JoinType.LEFT)

        if (userId != null) {
            predicates.add(cb.equal(userJoin.get<Int>("id"), userId))
        }

        if (status != null) {
            when (status) {
                "NONE" -> predicates.add(cb.isNull(reviewsJoin.get<String>("status")))
                "DONE" -> predicates.add(cb.isNotNull(reviewsJoin.get<String>("status")))
                else -> predicates.add(cb.equal(reviewsJoin.get<String>("status"), status))
            }
        }

        if (!committeeName.isNullOrEmpty()) {
            val committeeNames = committeeName.split(",").map { it.trim() }
            predicates.add(committeeJoin.get<String>("name").`in`(committeeNames))
        }

        if (!search.isNullOrEmpty()) {
            predicates.add(cb.like(root.get<String>("name"), "%$search%"))
        }

        query.distinct(true) // To avoid duplicates due to joins

        return cb.and(*predicates.toTypedArray())
    }
}
