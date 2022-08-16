package com.kkt.worthcalculation.controller

import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.SurveySportActCriteria
import com.kkt.worthcalculation.service.SurveyService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("api/v2/")
@RestController
@CrossOrigin
class SurveyController(
    val surveyService: SurveyService
) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @PostMapping("surveySportsActives")
    fun login(@RequestBody request: SurveySportActCriteria): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("Request Data = ${request.toString()}")
        return surveyService.getListSportAct(request)
    }
}


