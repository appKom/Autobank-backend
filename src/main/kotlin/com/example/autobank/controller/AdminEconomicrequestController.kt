package com.example.autobank.controller

import com.example.autobank.data.economicrequest.*
import com.example.autobank.service.EconomicrequestAdminService
import com.example.autobank.service.AuthenticationService
import com.example.autobank.service.EconomicRequestReviewService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.query.Param
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/api/admin/economicrequest")
@Tag(name = "Admin Economic Request Controller", description = "Endpoints for admin economic request management")
class AdminEconomicrequestController {

    @Autowired
    lateinit var economicrequestAdminService: EconomicrequestAdminService

    @Autowired
    lateinit var authenticationService: AuthenticationService

    @Autowired
    lateinit var economicRequestReviewService: EconomicRequestReviewService

    @Operation(summary = "Get all economic requests", description = "Retrieve a list of all economic requests")
    @GetMapping("/all")
    fun getAllEconomicrequests(@Param("page") from: Int = 0, @Param("count") count: Int = 10, @Param("status") status: String?, @Param("search") search: String?, @Param("sortOrder") sortOrder: String?, @Param("sortField") sortField: String?): ResponseEntity<EconomicrequestListResponseBody> {
        if (authenticationService.checkAdmin()) {
            return ResponseEntity.ok(economicrequestAdminService.getAll(from, count, status, search, sortField, sortOrder))
        }
        return ResponseEntity.status(403).build()
    }

    @Operation(summary = "Get an economic request by ID", description = "Retrieve a specific economic request by its ID")
    @GetMapping("/get/{id}")
    fun getEconomicrequest(@PathVariable id: String): ResponseEntity<CompleteEconomicrequest> {
        if (authenticationService.checkAdmin()) {
            return ResponseEntity.ok(economicrequestAdminService.getEconomicrequest(id))
        }
        return ResponseEntity.status(403).build()
    }

    @Operation(summary = "Review an economic request", description = "Create a review for a specific economic request")
    @PostMapping("/review")
    fun reviewEconomicrequest(@RequestBody reviewBody: EconomicrequestReviewRequestBody): ResponseEntity<EconomicrequestReviewResponseBody> {
        if (authenticationService.checkAdmin()) {
            try {
                return ResponseEntity.ok(economicRequestReviewService.createEconomicrequestReview(reviewBody))
            } catch (e: Exception) {
                println(e)
                return ResponseEntity.badRequest().build()
            }
        }
        return ResponseEntity.status(403).build()
    }
}
