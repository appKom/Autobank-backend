package com.example.autobank.service

import com.example.autobank.data.economicrequest.*
import com.example.autobank.data.models.Economicrequest
import com.example.autobank.data.models.EconomicRequestAttachment
import com.example.autobank.data.models.EconomicrequestInfo
import com.example.autobank.repository.EconomicRequestAttachmentRepository
import com.example.autobank.repository.EconomicrequestInfoRepositoryImpl
import com.example.autobank.repository.EconomicrequestRepository
import com.example.autobank.repository.specification.EconomicrequestInfoViewSpecification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime


@Service
class EconomicrequestService(
    private val economicrequestRepository: EconomicrequestRepository,
    private val onlineUserService: OnlineUserService,
    private val blobService: BlobService,
    private val economicrequestAttachmentRepository: EconomicRequestAttachmentRepository,
    private val economicrequestInfoRepository: EconomicrequestInfoRepositoryImpl,
    private val committeeService: CommitteeService,
    private val mailService: MailService,
    @Value("\${environment}") private val environment: String,
) {

    private val log = LoggerFactory.getLogger(EconomicrequestService::class.java)

    fun createEconomicrequest(requestBody: EconomicrequestRequestBody): EconomicrequestResponseBody {
        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")
        val dto = requestBody.economicrequest ?: throw Exception("Economic request not sent")

        val committee = dto.committeeId?.let {
            committeeService.getCommitteeById(it) ?: throw Exception("Committee not found")
        }

        val economicrequest = Economicrequest(
            id = "",
            subject = dto.subject ?: throw Exception("Subject not provided"),
            purpose = dto.purpose ?: throw Exception("Purpose not provided"),
            date = LocalDateTime.parse(dto.date ?: throw Exception("Date not provided")),
            description = dto.description ?: throw Exception("Description not provided"),
            amount = BigDecimal.valueOf(dto.amount ?: throw Exception("Amount not provided")),
            paymentDescription = dto.paymentDescription ?: throw Exception("Payment description not provided"),
            otherInformation = dto.otherInformation,
            onlinemail = dto.onlinemail,
            createdat = null,
            committee = committee,
            user = user,
        )

        val storedRequest = economicrequestRepository.save(economicrequest)
        log.info("Economic request saved with id=${storedRequest.id} for user=${user.id}")

        try {
            requestBody.attachments.forEach { file64 ->
                val imgname = blobService.uploadFile(file64)
                economicrequestAttachmentRepository.save(
                    EconomicRequestAttachment("", storedRequest, imgname)
                )
            }
        } catch (e: Exception) {
            log.error("Attachment upload failed for request id=${storedRequest.id}, rolling back: ${e.message}", e)
            economicrequestRepository.delete(storedRequest)
            throw e
        }

        val emailContent = """
            <h2>Ny søknad til Utstyrspotten</h2>
            <p><strong>Søker:</strong> ${user.fullname}</p>
            <p><strong>Søkerens e-post:</strong> ${user.email}</p>
            ${if (!storedRequest.onlinemail.isNullOrEmpty()) "<p><strong>Online-mail:</strong> ${storedRequest.onlinemail}</p>" else ""}
            <p><strong>Formål:</strong> ${storedRequest.purpose}</p>
            <p><strong>Beskrivelse:</strong> ${storedRequest.description}</p>
            <p><strong>Dato:</strong> ${storedRequest.date.toLocalDate()}</p>
            <p><strong>Beløp:</strong> ${storedRequest.amount}</p>
            <p><strong>Betalingsbeskrivelse:</strong> ${storedRequest.paymentDescription}</p>
            ${if (storedRequest.committee != null) "<p><strong>Komité:</strong> ${storedRequest.committee.name}</p>" else ""}
            ${if (!storedRequest.otherInformation.isNullOrEmpty()) "<p><strong>Annen informasjon:</strong> ${storedRequest.otherInformation}</p>" else ""}
        """.trimIndent()

        mailService.sendEmail(
            toEmail = storedRequest.onlinemail ?: user.email,
            subject = "[Utstyrspotten] Søknad mottatt: ${storedRequest.subject}",
            htmlBody = emailContent,
        )

        if (environment == "prod") {
            mailService.sendEmail(
                toEmail = "hovedstyret@online.ntnu.no",
                subject = "[Utstyrspotten] ${storedRequest.subject} - ${user.fullname}",
                htmlBody = emailContent,
            )
        }

        return EconomicrequestResponseBody()
    }

    fun getAllEconomicrequestsFromUser(from: Int, count: Int, status: String?, search: String?, sortField: String?, sortOrder: String?): EconomicrequestListResponseBody {
        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")

        val sort = if (!sortField.isNullOrEmpty()) {
            Sort.by(Sort.Direction.fromString(sortOrder ?: "ASC"), sortField)
        } else {
            Sort.by(Sort.Direction.DESC, "createdat")
        }
        val pageable = PageRequest.of(from, count, sort)

        val specification = EconomicrequestInfoViewSpecification(user.id, status, search)

        val page = economicrequestInfoRepository.findAll(specification, pageable)
        val total = page.totalElements

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
        return EconomicrequestListResponseBody(responseList.toTypedArray(), total)
    }

    fun getEconomicrequest(id: String): CompleteEconomicrequest? {
        val user = onlineUserService.getOnlineUser() ?: throw Exception("User not found")
        val info = economicrequestInfoRepository.findById(id)
        if (info == null || info.userId != user.id) {
            return null
        }
        return getCompleteEconomicrequest(info)
    }

    fun getCompleteEconomicrequest(info: EconomicrequestInfo): CompleteEconomicrequest {
        val attachments = economicrequestAttachmentRepository.findByEconomicrequestId(info.economicrequestId)
        val files = attachments.map { attachment ->
            attachment.name.split(".")[1].replace(":", "/") + "." + blobService.downloadImage(attachment.name)
        }

        return CompleteEconomicrequest(
            economicrequestId = info.economicrequestId,
            subject = info.subject,
            purpose = info.purpose,
            date = info.date,
            description = info.description,
            amount = info.amount,
            paymentDescription = info.paymentDescription,
            otherInformation = info.otherInformation,
            economicrequestCreatedAt = info.economicrequestCreatedAt,
            userFullname = info.userFullname,
            committeeName = info.committeeName,
            onlinemail = info.onlinemail,
            attachmentCount = info.attachmentCount.toInt(),
            latestReviewStatus = info.latestReviewStatus?.toString(),
            latestReviewCreatedAt = info.latestReviewCreatedAt,
            latestReviewComment = info.latestReviewComment,
            attachments = files,
        )
    }

    fun deleteEconomicrequest(id: Int) {
        economicrequestRepository.deleteById(id.toString())
    }
}