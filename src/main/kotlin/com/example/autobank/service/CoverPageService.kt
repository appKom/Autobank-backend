package com.example.autobank.service

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CoverPageService {

    data class CoverPageData(
        val name: String,
        val email: String,
        val committeeName: String,
        val date: String,
        val accountNumber: String?,
        val amount: String,
        val occasion: String,
        val type: String,
        val comment: String
    )

    /**
     * Generates a single PDF containing the cover page followed by all attachments
     * (images are embedded as pages, PDFs are appended page-by-page).
     */
    fun generateCombinedPdf(data: CoverPageData, attachments: List<Pair<String, ByteArray>>): ByteArray {
        val document = PDDocument()
        val sourceDocuments = mutableListOf<PDDocument>()

        try {
            addCoverPage(document, data)

            for ((filename, bytes) in attachments) {
                val lowerName = filename.lowercase()
                when {
                    lowerName.endsWith(".pdf") -> appendPdfPages(document, bytes, sourceDocuments)
                    lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png") ->
                        appendImagePage(document, filename, bytes)
                    else -> appendImagePage(document, filename, bytes)
                }
            }

            val outputStream = ByteArrayOutputStream()
            document.save(outputStream)
            return outputStream.toByteArray()
        } finally {
            sourceDocuments.forEach { it.close() }
            document.close()
        }
    }

    private fun addCoverPage(document: PDDocument, data: CoverPageData) {
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        val boldFont = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val regularFont = PDType1Font(Standard14Fonts.FontName.HELVETICA)

        val pageWidth = page.mediaBox.width
        val margin = 70f
        val labelX = margin
        val valueX = 230f
        var y = page.mediaBox.height - 80f
        val lineSpacing = 22f

        PDPageContentStream(document, page).use { cs ->
            // Title
            cs.beginText()
            cs.setFont(boldFont, 22f)
            cs.newLineAtOffset(labelX, y)
            cs.showText("Kvitteringsskjema")
            cs.endText()

            y -= 15f
            // Underline
            cs.setLineWidth(1f)
            cs.moveTo(labelX, y)
            cs.lineTo(labelX + 250f, y)
            cs.stroke()

            y -= 30f

            // Field rows
            val fields = mutableListOf(
                "Navn:" to data.name,
                "Epost:" to data.email,
                "Ansvarlig enhet:" to data.committeeName,
                "Dato:" to data.date,
            )
            if (!data.accountNumber.isNullOrBlank()) {
                fields.add("Kontonummer:" to formatAccountNumber(data.accountNumber))
            }
            fields.addAll(listOf(
                "Beløp:" to data.amount,
                "Anledning:" to data.occasion,
                "Type:" to data.type,
            ))

            for ((label, value) in fields) {
                cs.beginText()
                cs.setFont(regularFont, 11f)
                cs.newLineAtOffset(labelX, y)
                cs.showText(label)
                cs.endText()

                cs.beginText()
                cs.setFont(regularFont, 11f)
                cs.newLineAtOffset(valueX, y)
                cs.showText(safePDF(value, regularFont, false))
                cs.endText()

                y -= lineSpacing
            }

            // Comment section
            y -= 15f
            cs.beginText()
            cs.setFont(regularFont, 11f)
            cs.newLineAtOffset(labelX, y)
            cs.showText("Kommentar")
            cs.endText()

            y -= 25f

            // Wrap comment text across multiple lines
            val maxLineWidth = pageWidth - margin * 2
            for (line in wrapText(safePDF(data.comment, regularFont, true), regularFont, 11f, maxLineWidth)) {
                cs.beginText()
                cs.setFont(regularFont, 11f)
                cs.newLineAtOffset(labelX, y)
                cs.showText(line)
                cs.endText()
                y -= lineSpacing
            }
        }
    }

    private fun appendPdfPages(targetDocument: PDDocument, pdfBytes: ByteArray, sourceDocuments: MutableList<PDDocument>) {
        val sourceDoc = Loader.loadPDF(pdfBytes)
        sourceDocuments.add(sourceDoc)
        for (page in sourceDoc.pages) {
            targetDocument.importPage(page)
        }
    }

    private fun appendImagePage(targetDocument: PDDocument, filename: String, imageBytes: ByteArray) {
        val page = PDPage(PDRectangle.A4)
        targetDocument.addPage(page)

        val image = PDImageXObject.createFromByteArray(targetDocument, imageBytes, filename)

        val pageWidth = page.mediaBox.width
        val pageHeight = page.mediaBox.height
        val margin = 40f
        val availableWidth = pageWidth - margin * 2
        val availableHeight = pageHeight - margin * 2

        // Scale image to fit the page while maintaining aspect ratio
        val scaleX = availableWidth / image.width
        val scaleY = availableHeight / image.height
        val scale = minOf(scaleX, scaleY, 1f) // don't upscale

        val drawWidth = image.width * scale
        val drawHeight = image.height * scale
        val x = (pageWidth - drawWidth) / 2
        val y = (pageHeight - drawHeight) / 2

        PDPageContentStream(targetDocument, page).use { cs ->
            cs.drawImage(image, x, y, drawWidth, drawHeight)
        }
    }

    /**
     * Formats account number as "xxxx xx xxxxx" (4-2-5 grouping).
     */
    private fun formatAccountNumber(accountNumber: String?): String {
        if (accountNumber.isNullOrBlank()) return ""
        val digits = accountNumber.replace(" ", "").replace(".", "")
        if (digits.length != 11) return accountNumber
        return "${digits.substring(0, 4)} ${digits.substring(4, 6)} ${digits.substring(6, 11)}"
    }

    private fun safePDF(text: String, font: PDType1Font, keepNewLines: Boolean): String {
        val stringBuilder = StringBuilder()
        var i = 0
        while (i < text.length){
            val cp = text.codePointAt(i)
            i += Character.charCount(cp)
            when {
                cp == '\n'.code && keepNewLines -> stringBuilder.append('\n')
                Character.isWhitespace(cp) || Character.isSpaceChar(cp) -> stringBuilder.append(' ')
                else -> {
                    val ch = String(Character.toChars(cp))
                    try {
                        font.encode(ch)
                        stringBuilder.append(ch)
                    } catch (e: Exception) {
                        stringBuilder.append('?')
                    }
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun wrapText(text: String, font: PDType1Font, fontSize: Float, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        for (paragraph in text.split("\n")) {
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val width = font.getStringWidth(testLine) / 1000f * fontSize
                if (width > maxWidth && currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                    currentLine = StringBuilder(word)
                } else {
                    currentLine = StringBuilder(testLine)
                }
            }
            if (currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
            }
        }
        return lines
    }
}
