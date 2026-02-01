package com.example.autobank.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.io.ByteArrayOutputStream
import java.util.Properties

@Service
class MailService(
    @Value("\${aws.access-key-id}") private val accessKeyId: String,
    @Value("\${aws.secret-access-key}") private val secretAccessKey: String,
) {

    private val sesClient: SesClient = SesClient.builder()
        .region(Region.EU_NORTH_1)
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            )
        )
        .build()

    /**
     * Sends an email via AWS SES.
     * If [attachments] is provided, it sends a Raw MIME message.
     */
    fun sendEmail(
        toEmail: String,
        subject: String,
        htmlBody: String,
        attachments: List<Pair<String, ByteArray>> = emptyList()
    ) {
        val session = Session.getDefaultInstance(Properties())
        val message = MimeMessage(session)

        message.setSubject(subject, "UTF-8")
        message.setFrom(InternetAddress("kvittering@online.ntnu.no"))
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))

        // Create a multipart container to hold the text and the files
        val multipart = MimeMultipart()

        // 1. Add the HTML content
        val htmlPart = MimeBodyPart()
        htmlPart.setContent(htmlBody, "text/html; charset=UTF-8")
        multipart.addBodyPart(htmlPart)

        // 2. Add any attachments
        attachments.forEach { (fileName, data) ->
            val attachmentPart = MimeBodyPart()
            attachmentPart.fileName = fileName
            attachmentPart.setContent(data, "application/octet-stream")
            multipart.addBodyPart(attachmentPart)
        }

        message.setContent(multipart)

        // Convert the MimeMessage to a format AWS SES understands
        val outputStream = ByteArrayOutputStream()
        message.writeTo(outputStream)

        val rawMessage = RawMessage.builder()
            .data(SdkBytes.fromByteArray(outputStream.toByteArray()))
            .build()

        val request = SendRawEmailRequest.builder()
            .rawMessage(rawMessage)
            .build()

        try {
            sesClient.sendRawEmail(request)
            println("Email sent successfully to $toEmail ${if(attachments.isNotEmpty()) "with attachments" else ""}")
        } catch (e: SesException) {
            println("Failed to send email via SES: ${e.awsErrorDetails().errorMessage()}")
            throw e
        }
    }
}