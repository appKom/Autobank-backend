package com.example.autobank.repository

import com.example.autobank.data.models.Committee
import com.example.autobank.data.models.Economicrequest
import com.example.autobank.data.models.EconomicrequestInfo
import com.example.autobank.data.models.EconomicRequestReview
import com.example.autobank.data.models.EconomicRequestAttachment
import com.example.autobank.data.user.OnlineUser
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Root
import org.springframework.data.domain.PageImpl
import java.math.BigDecimal
import java.time.LocalDateTime

private data class EconomicrequestQueryComponents(
    val cb: CriteriaBuilder,
    val cq: CriteriaQuery<EconomicrequestInfo>,
    val root: Root<Economicrequest>
)

@Repository
class EconomicrequestInfoRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : EconomicrequestInfoRepository {

    private fun getJoinedQuery(): EconomicrequestQueryComponents {
        val cb = em.criteriaBuilder
        val cq = cb.createQuery(EconomicrequestInfo::class.java)
        val root = cq.from(Economicrequest::class.java)

        val userJoin = root.join<Economicrequest, OnlineUser>("user")
        val committeeJoin = root.join<Economicrequest, Committee>("committee", JoinType.LEFT)
        val attachmentsJoin = root.join<Economicrequest, EconomicRequestAttachment>("attachments", JoinType.LEFT)
        val reviewsJoin = root.join<Economicrequest, EconomicRequestReview>("reviews", JoinType.LEFT)

        cq.multiselect(
            root.get<String>("id"),
            root.get<String>("subject"),
            root.get<String>("purpose"),
            root.get<LocalDateTime>("date"),
            root.get<String>("description"),
            root.get<BigDecimal>("amount"),
            root.get<String>("paymentDescription"),
            root.get<String>("otherInformation"),
            root.get<String>("onlinemail"),
            root.get<LocalDateTime>("createdat"),
            userJoin.get<String>("fullname"),
            userJoin.get<String>("id"),
            committeeJoin.get<String>("name"),
            cb.countDistinct(attachmentsJoin.get<Int>("id")),
            reviewsJoin.get<String>("status"),
            reviewsJoin.get<LocalDateTime>("createdat"),
            reviewsJoin.get<String>("comment"),
        )

        cq.groupBy(
            root.get<String>("id"),
            root.get<String>("subject"),
            root.get<String>("purpose"),
            root.get<LocalDateTime>("date"),
            root.get<String>("description"),
            root.get<BigDecimal>("amount"),
            root.get<String>("paymentDescription"),
            root.get<String>("otherInformation"),
            root.get<String>("onlinemail"),
            root.get<LocalDateTime>("createdat"),
            userJoin.get<String>("fullname"),
            userJoin.get<String>("id"),
            committeeJoin.get<String>("name"),
            reviewsJoin.get<String>("status"),
            reviewsJoin.get<LocalDateTime>("createdat"),
            reviewsJoin.get<String>("comment"),
        )

        return EconomicrequestQueryComponents(cb, cq, root)
    }

    override fun findById(id: String): EconomicrequestInfo? {
        val (cb, cq, root) = getJoinedQuery()
        cq.where(cb.equal(root.get<String>("id"), id))

        val query = em.createQuery(cq)
        query.maxResults = 1

        return query.resultList.firstOrNull()
    }

    override fun findAll(spec: Specification<Economicrequest>, pageable: Pageable): Page<EconomicrequestInfo> {
        val (cb, cq, root) = getJoinedQuery()

        cq.where(spec.toPredicate(root, cq, cb))

        if (pageable.sort.isSorted) {
            val orders = pageable.sort.map { order ->
                if (order.isAscending) {
                    cb.asc(root.get<Any>(order.property))
                } else {
                    cb.desc(root.get<Any>(order.property))
                }
            }.toList()
            cq.orderBy(orders)
        }

        val query = em.createQuery(cq)
        query.firstResult = pageable.offset.toInt()
        query.maxResults = pageable.pageSize

        val content = query.resultList

        val countQuery = cb.createQuery(Long::class.java)
        val countRoot = countQuery.from(Economicrequest::class.java)
        countQuery.select(cb.countDistinct(countRoot))
        countQuery.where(spec.toPredicate(countRoot, countQuery, cb))
        val total = em.createQuery(countQuery).singleResult

        return PageImpl(content, pageable, total)
    }
}
