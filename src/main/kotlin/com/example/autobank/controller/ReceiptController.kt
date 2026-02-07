package com.example.autobank.controller

import com.example.autobank.data.receipt.*
import com.example.autobank.service.ReceiptService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag


@RestController
@RequestMapping("/api/receipt")
@Tag(name = "Receipt Controller", description = "Endpoints for user receipt management")
class ReceiptController {

    @Autowired
    lateinit var receiptService: ReceiptService

    @Operation(summary = "Create a new receipt", description = "Create a new receipt for the authenticated user")
    @PostMapping("/create")
    fun createReceipt(@RequestBody receipt: ReceiptRequestBody): ResponseEntity<ReceiptResponseBody> {
        return try {
            val res = receiptService.createReceipt(receipt)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            println("Receipt creation failed: ${e.message}")
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Get all receipts for user", description = "Retrieve a list of all receipts for the authenticated user")
    @GetMapping("/getall")
    fun getAllReceipts(@Param("page") from: Int = 0, @Param("count") count: Int = 10, @Param("status") status: String?, @Param("committee") committee: String?, @Param("search") search: String?, @Param("sortOrder") sortOrder: String?, @Param("sortField") sortField: String?): ResponseEntity<ReceiptListResponseBody> {
        return try {
            val res = receiptService.getAllReceiptsFromUser(from, count, status, committee, search, sortField, sortOrder)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            e.printStackTrace()
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Get a receipt by ID", description = "Retrieve a specific receipt by its ID")
    @GetMapping("/get/{id}")
    fun getReceipt(@PathVariable id: String): ResponseEntity<CompleteReceipt> {
        return try {
            val res = receiptService.getReceipt(id)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }


}