package com.kkt.worthcalculation.controller;

import com.kkt.worthcalculation.service.ReportService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.Valid

@RequestMapping("api/v2/report")
@RestController
public class ReportController(val service: ReportService) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @GetMapping("/general")
    fun getGeneralReport(@Valid @RequestParam startDate: String, @Valid @RequestParam endDate: String): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        return service.downloadReportGeneral(startDate, endDate)
    }

    @GetMapping("/permission")
    fun getPermissionReport(@Valid @RequestParam startDate: String, @Valid @RequestParam endDate: String): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        return service.downloadReportPermission(startDate, endDate)
    }

    @GetMapping("/login")
    fun getLoginReport(@Valid @RequestParam startDate: String, @Valid @RequestParam endDate: String): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        return service.downloadReportLogin(startDate, endDate)
    }


}
