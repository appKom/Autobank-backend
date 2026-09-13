package com.example.autobank.controller

import com.example.autobank.data.economicrequest.*
import com.example.autobank.service.EconomicrequestService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

data class ErrorResponse(val error: String)

@RestController
@RequestMapping("/api/economicrequest")
@Tag(name = "Economic Request Controller", description = "Endpoints for user economic request management (utstyrspotten)")
class EconomicrequestController {

    private val log = LoggerFactory.getLogger(EconomicrequestController::class.java)

    @Autowired
    lateinit var economicrequestService: EconomicrequestService

    @Operation(summary = "Create a new economic request", description = "Create a new economic request for the authenticated user")
    @PostMapping("/create")
    fun createEconomicrequest(@RequestBody requestBody: EconomicrequestRequestBody): ResponseEntity<Any> {
        return try {
            val res = economicrequestService.createEconomicrequest(requestBody)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            log.error("Economic request creation failed: ${e.message}", e)
            ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "Unknown error"))
        }
    }

    @Operation(summary = "Get all economic requests for user", description = "Retrieve a list of all economic requests for the authenticated user")
    @GetMapping("/getall")
    fun getAllEconomicrequests(@Param("page") from: Int = 0, @Param("count") count: Int = 10, @Param("status") status: String?, @Param("search") search: String?, @Param("sortOrder") sortOrder: String?, @Param("sortField") sortField: String?): ResponseEntity<Any> {
        return try {
            val res = economicrequestService.getAllEconomicrequestsFromUser(from, count, status, search, sortField, sortOrder)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            log.error("Failed to fetch economic requests: ${e.message}", e)
            ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "Unknown error"))
        }
    }

    @Operation(summary = "Get an economic request by ID", description = "Retrieve a specific economic request by its ID")
    @GetMapping("/get/{id}")
    fun getEconomicrequest(@PathVariable id: String): ResponseEntity<Any> {
        return try {
            val res = economicrequestService.getEconomicrequest(id)
            ResponseEntity.ok(res)
        } catch (e: Exception) {
            log.error("Failed to fetch economic request $id: ${e.message}", e)
            ResponseEntity.badRequest().body(ErrorResponse(e.message ?: "Unknown error"))
        }
    }
}