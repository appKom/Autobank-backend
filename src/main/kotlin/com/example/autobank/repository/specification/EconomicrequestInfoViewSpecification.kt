package com.example.autobank.repository.specification

import com.example.autobank.data.models.Economicrequest
import com.example.autobank.data.models.EconomicRequestReview
import com.example.autobank.data.user.OnlineUser
import jakarta.persistence.criteria.*
import org.springframework.data.jpa.domain.Specification

class EconomicrequestInfoViewSpecification(
    private val userId: String?,
    private val status: String?,
    private val search: String?
) : Specification<Economicrequest> {

    override fun toPredicate(
        root: Root<Economicrequest>,
        query: CriteriaQuery<*>,
        cb: CriteriaBuilder
    ): Predicate? {
        val predicates = mutableListOf<Predicate>()

        val userJoin: Join<Economicrequest, OnlineUser> = root.join("user")
        val reviewsJoin: SetJoin<Economicrequest, EconomicRequestReview> = root.joinSet("reviews", JoinType.LEFT)

        if (userId != null) {
            predicates.add(cb.equal(userJoin.get<String>("id"), userId))
        }

        if (status != null) {
            when (status) {
                "NONE" -> predicates.add(cb.isNull(reviewsJoin.get<String>("status")))
                "DONE" -> predicates.add(cb.isNotNull(reviewsJoin.get<String>("status")))
                else -> predicates.add(cb.equal(reviewsJoin.get<String>("status"), status))
            }
        }

        if (!search.isNullOrEmpty()) {
            predicates.add(cb.like(root.get<String>("subject"), "%$search%"))
        }

        query.distinct(true)

        return cb.and(*predicates.toTypedArray())
    }
}
