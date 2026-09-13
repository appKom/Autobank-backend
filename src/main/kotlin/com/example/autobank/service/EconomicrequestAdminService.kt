package com.example.autobank.service

import com.example.autobank.data.economicrequest.*
import com.example.autobank.repository.EconomicrequestInfoRepositoryImpl
import com.example.autobank.repository.specification.EconomicrequestInfoViewSpecification
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class EconomicrequestAdminService(
    private val economicrequestInfoRepository: EconomicrequestInfoRepositoryImpl,
    private val economicrequestService: EconomicrequestService,
) {

    fun getAll(from: Int, count: Int, status: String?, search: String?, sortField: String?, sortOrder: String?): EconomicrequestListResponseBody {
        val sort = if (!sortField.isNullOrEmpty()) {
            Sort.by(Sort.Direction.fromString(sortOrder ?: "ASC"), sortField)
        } else {
            Sort.by(Sort.Direction.DESC, "createdat")
        }

        val pageable = PageRequest.of(from, count, sort)
        val specification = EconomicrequestInfoViewSpecification(null, status, search)

        val page = economicrequestInfoRepository.findAll(specification, pageable)

        val responseList = page.toList().map { info ->
            EconomicrequestInfoResponseBody(
                economicrequestId = info.economicrequestId,
                subject = info.subject,
                purpose = info.purpose,
                description = info.description,
                amount = info.amount.toString(),
                economicrequestCreatedAt = info.economicrequestCreatedAt.toString(),
                userFullname = info.userFullname,
                committeeName = info.committeeName,
                onlinemail = info.onlinemail,
                attachmentCount = info.attachmentCount.toInt(),
                latestReviewStatus = info.latestReviewStatus?.toString(),
                latestReviewCreatedAt = info.latestReviewCreatedAt?.toString(),
                latestReviewComment = info.latestReviewComment,
            )
        }

        val total = page.totalElements
        return EconomicrequestListResponseBody(responseList.toTypedArray(), total)
    }

    fun getEconomicrequest(id: String): CompleteEconomicrequest? {
        val info = economicrequestInfoRepository.findById(id) ?: return null
        return economicrequestService.getCompleteEconomicrequest(info)
    }
}
