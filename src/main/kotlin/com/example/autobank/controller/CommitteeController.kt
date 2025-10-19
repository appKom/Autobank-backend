package com.example.autobank.controller

import com.example.autobank.data.models.Committee
import com.example.autobank.data.user.UserCommitteeResponseBody
import com.example.autobank.service.CommitteeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/api/committee")
@Tag(name = "Committee Controller", description = "Endpoints for managing committees")
class CommitteeController(
    ) {

    @Autowired
    lateinit var committeeService: CommitteeService

    @Operation(summary = "Get all committees", description = "Retrieve a list of all committees")
    @GetMapping("/all")
    fun getAllCommittees(): ResponseEntity<List<Committee>> {
        return try {
            val committees = committeeService.getAllCommittees()
            ResponseEntity.ok(committees)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }

    @Operation(summary = "Get user and committees", description = "Retrieve the authenticated user along with their associated committees")
    @GetMapping("/user")
    fun getUserAndCommittees(): ResponseEntity<UserCommitteeResponseBody> {
        return try {
            val userandcommittees = committeeService.getUserAndCommittees()
            ResponseEntity.ok(userandcommittees)
        } catch (e: Exception) {
            ResponseEntity.badRequest().build()
        }
    }
}