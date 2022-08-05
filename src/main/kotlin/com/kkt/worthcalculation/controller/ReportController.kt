package com.kkt.worthcalculation.controller;

import com.kkt.worthcalculation.service.ReportService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
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
    fun getGeneralReport(): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        return service.downloadReportGeneral()
    }

    @GetMapping("/permission")
    fun getListImport(@Valid @RequestParam sportTournamentId: String): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTournamentId: $sportTournamentId")
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Report-ข้อมูลพื้นฐาน.xlsx")
            .body(null)
    }


}
