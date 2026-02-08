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
        // Log the first 100 characters to debug format issues
        println("Attachment data prefix: ${file64.take(100)}")
        
        // Parse the attachment - support both formats:
        // 1. Data URL format: "data:image/png;base64,iVBORw0KGgo..."
        // 2. Legacy format: "image/png.iVBORw0KGgo..." or "image:png.iVBORw0KGgo..."
        val (mimeType, base64Data) = parseAttachment(file64)
        
        println("Attempting to upload file with MIME type: $mimeType")
        
        if (mimeType !in ALLOWED_MIME_TYPES) {
            println("Rejected file with unsupported MIME type: $mimeType")
            throw Exception("Invalid file type: $mimeType. Allowed types: JPEG, PNG, or PDF")
        }

        val bytearray: ByteArray = try {
            Base64.getDecoder().decode(base64Data)
        } catch (e: Exception) {
            println("ERROR: Failed to decode base64 data: ${e.message}")
            throw Exception("Failed to decode attachment: ${e.message}")
        }
        println("Original file size: ${bytearray.size} bytes")
        
        // Resize images, keep PDFs as-is
        val file: ByteArray = if (mimeType.startsWith("image/")) {
            try {
                resizeImage(bytearray, mimeType.split("/")[1])
            } catch (e: Exception) {
                println("Error resizing image: ${e.message}")
                throw Exception("Failed to process image: ${e.message}")
            }
        } else {
            bytearray
        }
        
        println("Processed file size: ${file.size} bytes")

        if (file.size > maxFileSize) {
            throw Exception("File size is too large: ${file.size} bytes (max: $maxFileSize bytes)")
        }

        val extension = when(mimeType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "application/pdf" -> "pdf"
            else -> "bin"
        }

        // 2. Create the legacy part (For Frontend compatibility)
        // The frontend likely parses "image:png" from the name
        val legacyMimePart = mimeType.replace('/', ':')

        // 3. Combine them: UUID + Legacy Part + Standard Extension
        // Result: "550e8400-e29b.image:png.png"
        val filename = "${UUID.randomUUID()}.$legacyMimePart.$extension"

        val blobClient = blobContainerClient.getBlobClient(filename)
        blobClient.upload(file.inputStream(), file.size.toLong())

        println("Uploaded file: $filename")
        return filename
    }

    /**
     * Parse attachment data from either format:
     * 1. Data URL format: "data:image/png;base64,iVBORw0KGgo..."
     * 2. Legacy format: "image/png.iVBORw0KGgo..." or "image:png.iVBORw0KGgo..."
     */
    private fun parseAttachment(file64: String): Pair<String, String> {
        val (rawMime, base64Data) = when {
            // Standard data URL format
            file64.startsWith("data:") && file64.contains(";base64,") -> {
                val mimeType = file64.substringAfter("data:").substringBefore(";base64")
                val data = file64.substringAfter("base64,")
                Pair(mimeType, data)
            }
            // Legacy/Email format
            file64.contains(".") -> {
                val firstDotIndex = file64.indexOf(".")
                val mimeTypePart = file64.substring(0, firstDotIndex)
                val data = file64.substring(firstDotIndex + 1)
                Pair(mimeTypePart, data)
            }
            else -> throw Exception("Invalid attachment format")
        }

        // NORMALIZATION STEP:
        // Convert 'image.jpeg' or 'application-pdf' into 'image/jpeg' or 'application/pdf'
        val normalizedMime = rawMime
            .replace(".", "/")
            .replace("-", "/")
            .replace(":", "/")
            .lowercase()

        return Pair(normalizedMime, base64Data)
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