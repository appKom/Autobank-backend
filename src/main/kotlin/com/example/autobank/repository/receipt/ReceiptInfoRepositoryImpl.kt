package com.example.autobank.repository.receipt

import com.example.autobank.data.models.Receipt
import com.example.autobank.data.models.ReceiptInfo
import com.example.autobank.data.models.ReceiptReview
import com.example.autobank.data.models.Committee
import com.example.autobank.data.user.OnlineUser

import com.example.autobank.data.models.Attachment
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

private data class ReceiptQueryComponents(
    val cb: CriteriaBuilder,
    val cq: CriteriaQuery<ReceiptInfo>,
    val root: Root<Receipt>
)

@Repository
class ReceiptInfoRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : ReceiptInfoRepository {

    private fun getJoinedQuery(): ReceiptQueryComponents {

        val cb = em.criteriaBuilder
        val cq = cb.createQuery(ReceiptInfo::class.java)
        val root = cq.from(Receipt::class.java)

        // Joins
        val userJoin = root.join<Receipt, OnlineUser>("user")
        val committeeJoin = root.join<Receipt, Committee>("committee", JoinType.LEFT)
        val attachmentsJoin = root.join<Receipt, Attachment>("attachments", JoinType.LEFT)
        val reviewsJoin = root.join<Receipt, ReceiptReview>("reviews", JoinType.LEFT)


        cq.multiselect(
            root.get<String>("id"),                                            // receiptId
            root.get<BigDecimal>("amount"),                                 // amount
            root.get<String>("name"),                                       // receiptName
            root.get<String>("description"),                                // receiptDescription
            root.get<LocalDateTime>("createdat"),                           // receiptCreatedAt
            committeeJoin.get<String>("name"),                              // committeeName
            userJoin.get<String>("fullname"),                               // userFullname
            root.get<String>("account_number"),                             // accountNumber
            root.get<String>("card_number"),                                // cardNumber
            userJoin.get<String>("id"),                                        // userId
            cb.countDistinct(attachmentsJoin.get<Int>("id")),               // attachmentCount
            reviewsJoin.get<String>("status"),                              // latestReviewStatus
            reviewsJoin.get<LocalDateTime>("createdat"),                    // latestReviewCreatedAt
            reviewsJoin.get<String>("comment")                              // latestReviewComment
        )

        cq.groupBy(
            root.get<String>("id"),
            root.get<BigDecimal>("amount"),
            root.get<String>("name"),
            root.get<String>("description"),
            root.get<LocalDateTime>("createdat"),
            root.get<String>("card_number"),
            root.get<String>("account_number"),
            committeeJoin.get<String>("name"),
            userJoin.get<String>("fullname"),
            userJoin.get<String>("id"),
            reviewsJoin.get<String>("status"),
            reviewsJoin.get<LocalDateTime>("createdat"),
            reviewsJoin.get<String>("comment")
        )

        return ReceiptQueryComponents(cb, cq, root)


    }

    override fun findById(id: String): ReceiptInfo? {
        val (cb, cq, root) = getJoinedQuery()
        cq.where(cb.equal(root.get<String>("id"), id))

        val query = em.createQuery(cq)
        query.maxResults = 1

        return query.resultList.firstOrNull()

    }

    override fun findAll(spec: Specification<Receipt>, pageable: Pageable): Page<ReceiptInfo> {

        val (cb, cq, root) = getJoinedQuery()

        cq.where(spec.toPredicate(root, cq, cb))

        // Apply sort from Pageable
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
        val countRoot = countQuery.from(Receipt::class.java)
        countQuery.select(cb.countDistinct(countRoot))
        countQuery.where(spec.toPredicate(countRoot, countQuery, cb))
        val total = em.createQuery(countQuery).singleResult

        return PageImpl(content, pageable, total)
    }


}
