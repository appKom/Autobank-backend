package com.example.autobank.controller

import com.example.autobank.data.models.Economicrequest
import com.example.autobank.service.AuthenticationService
import com.example.autobank.service.EconomicrequestService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/api/economicrequest")
class EconomicrequestController() {
    @Autowired
    lateinit var economicrequestService: EconomicrequestService;

    @Autowired
    lateinit var authenticationService: AuthenticationService;


    //@GetMapping("/get/{id}")
    fun getEconomicrequest(@PathVariable id: Int): Economicrequest {
        return economicrequestService.getEconomicrequest(id);
    }

}