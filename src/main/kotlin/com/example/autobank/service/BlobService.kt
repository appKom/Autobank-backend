package com.example.autobank.service

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClientBuilder
import net.coobird.thumbnailator.Thumbnails
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*

val ALLOWED_MIME_TYPES = listOf("image/jpeg", "image/png", "application/pdf", "image/jpg")
const val imageResizeWidth = 1000
const val imageResizeHeight = 800
const val maxFileSize = 5 * 1024 * 1024

@Service
class BlobService(
    @Value("\${azure.storage.connection-string}") private val connectionString: String? = null,
    @Value("\${azure.storage.container-name}") private val containerName: String? = null
) {

    private val blobContainerClient: BlobContainerClient =
        BlobServiceClientBuilder().connectionString(connectionString).buildClient()
            .getBlobContainerClient(containerName)

    fun uploadFile(file64: String): String {
        val base64Data = file64.substringAfter("base64,")
        val mimeType = file64.substringAfter("data:").substringBefore(";base64")
        if (mimeType !in ALLOWED_MIME_TYPES) {
            throw Exception("Invalid file type")
        }

        val bytearray: ByteArray = Base64.getDecoder().decode(base64Data)
        val file: ByteArray = if (mimeType.contains("image")) {
            resizeImage(bytearray, mimeType.split("/")[1])
        } else {
            bytearray
        }

        if (file.size > maxFileSize) {
            throw Exception("File size is too large")
        } else {
            val extension = mimeType.substringAfter("/")
            val filename = "${UUID.randomUUID()}.$extension".replace("\\s".toRegex(), "_")

            val blobClient = blobContainerClient.getBlobClient(filename)
            blobClient.upload(file.inputStream(), file.size.toLong())

            println("Uploaded image to $filename")
            return filename
        }
    }

    private fun resizeImage(imageData: ByteArray, type: String): ByteArray {
        ByteArrayInputStream(imageData).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Thumbnails.of(inputStream)
                    .size(imageResizeWidth, imageResizeHeight)
                    .keepAspectRatio(true)
                    .outputFormat(type)
                    .toOutputStream(outputStream)

                return outputStream.toByteArray()
            }
        }
    }

    fun downloadImage(fileName: String): String {
        val blobClient = blobContainerClient.getBlobClient(fileName)

        ByteArrayOutputStream().use { outputStream ->
            blobClient.downloadStream(outputStream)

            val imageData: ByteArray = outputStream.toByteArray()
            return Base64.getEncoder().encodeToString(imageData)
        }
    }



}