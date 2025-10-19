package com.example.autobank.controller;

import com.example.autobank.data.receipt.CompleteReceipt
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.example.autobank.service.ReceiptAdminService;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import com.example.autobank.service.AuthenticationService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import com.example.autobank.data.ReceiptReviewRequestBody
import com.example.autobank.data.receipt.ReceiptListResponseBody
import com.example.autobank.data.models.ReceiptReview
import com.example.autobank.data.receipt.ReceiptReviewResponseBody
import com.example.autobank.service.ReceiptReviewService
import org.springframework.data.repository.query.Param
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/api/admin/receipt")
@Tag(name = "Admin Receipt Controller", description = "Endpoints for admin receipt management")
class AdminReceiptController {

    @Autowired
    lateinit var receiptAdminService: ReceiptAdminService

    @Autowired
    lateinit var authenticationService: AuthenticationService

    @Autowired
    lateinit var receiptReviewService: ReceiptReviewService

    @Operation(summary = "Get all receipts", description = "Retrieve a list of all receipts")
    @GetMapping("/all")
    fun getAllReceipts(@Param("page") from: Int = 0, @Param("count") count: Int = 10, @Param("status") status: String?, @Param("committee") committee: String?, @Param("search") search: String?, @Param("sortOrder") sortOrder: String?, @Param("sortField") sortField: String?): ResponseEntity<ReceiptListResponseBody> {
        if (authenticationService.checkAdmin()) {
            return ResponseEntity.ok(receiptAdminService.getAll(from, count, status, committee, search, sortField, sortOrder))
        }
        return ResponseEntity.status(403).build()

    }

    @Operation(summary = "Get a receipt by ID", description = "Retrieve a specific receipt by its ID")
    @GetMapping("/get/{id}")
    fun getReceipt(@PathVariable id: String): ResponseEntity<CompleteReceipt> {
        if (authenticationService.checkAdmin()) {
            return ResponseEntity.ok(receiptAdminService.getReceipt(id))

        }
        return ResponseEntity.status(403).build()


    }

    @Operation(summary = "Review a receipt", description = "Create a review for a specific receipt")
    @PostMapping("/review")
    fun reviewReceipt(@RequestBody reviewBody: ReceiptReviewRequestBody): ResponseEntity<ReceiptReviewResponseBody> {
        if (authenticationService.checkAdmin()) {
            try {
                return ResponseEntity.ok(receiptReviewService.createReceiptReview(reviewBody));
            } catch (e: Exception) {
                println(e)
                return ResponseEntity.badRequest().build()
            }
        }
        return ResponseEntity.status(403).build()

    }


}
