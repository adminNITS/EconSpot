package com.kkt.worthcalculation.controller

import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.RequestCompareCriteria
import com.kkt.worthcalculation.service.SurveyCompareService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import javax.validation.Valid


@RequestMapping("api/v2/surveyCompare")
@RestController
class SurveyCompareController(val service: SurveyCompareService) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @PostMapping("/import")
    fun importExcel(@Valid @RequestParam sportTournamentId: String, @RequestParam("uploadfile") file: MultipartFile): ResponseEntity<ResponseModel> {
        logger.info("request: $sportTournamentId ${file.originalFilename}")
        if (file.contentType != "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            return ResponseEntity.badRequest().body(
                ResponseModel(
                    message = "File type support only xlsx",
                    status = "error",
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        val isSuccess: Boolean = service.processExcel(sportTournamentId, file)
        val response: ResponseModel = if (isSuccess)
            ResponseModel(
                message = "Import data success",
                status = "ok",
                timestamp = LocalDateTime.now(),
                data = null,
                pagination = null
            )
        else
            ResponseModel(
                message = "Error import excel",
                status = "error",
                timestamp = LocalDateTime.now(),
                data = null,
                pagination = null
            )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/")
    fun getListImport(@Valid @RequestParam sportTournamentId: String): ResponseEntity<ResponseModel> {
        logger.info("request: $sportTournamentId")
        return service.getListImport(sportTournamentId)
    }

    @GetMapping("/compare")
    fun getCompare(@Valid @RequestBody requestModel: RequestCompareCriteria): ResponseEntity<ResponseModel> {
        logger.info("request Tournament A: ${requestModel.tournamentA.tournamentId}")
        logger.info("request Tournament B: ${requestModel.tournamentB.tournamentId}")
        return service.compareTournament(requestModel)
    }
}