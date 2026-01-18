package com.example.autobank.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.*

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

    fun sendEmail(toEmail: String, subject: String, htmlBody: String) {
        val destination = Destination.builder()
            .toAddresses(toEmail)
            .build()

        val content = Content.builder()
            .data(subject)
            .charset("UTF-8")
            .build()

        val body = Body.builder()
            .html(Content.builder().data(htmlBody).charset("UTF-8").build())
            .build()

        val message = Message.builder()
            .subject(content)
            .body(body)
            .build()

        val request = SendEmailRequest.builder()
            .destination(destination)
            .message(message)
            .source("kvittering@online.ntnu.no") 
            .build()

        try {
            sesClient.sendEmail(request)
            println("Email sent successfully to $toEmail")
        } catch (e: SesException) {
            println("Failed to send email: ${e.awsErrorDetails().errorMessage()}")
            throw e
        }
    }
}