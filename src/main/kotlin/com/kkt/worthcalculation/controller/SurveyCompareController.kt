package com.kkt.worthcalculation.controller

import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.RequestCompareCriteria
import com.kkt.worthcalculation.service.SurveyCompareService
import com.kkt.worthcalculation.util.Util
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*
import javax.validation.Valid


@RequestMapping("api/v2/worth")
@RestController
@CrossOrigin
class SurveyCompareController(val service: SurveyCompareService) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @PostMapping("/excel/import")
    fun importExcel(@Valid @RequestParam sportTournamentId: String, @RequestParam("uploadfile") file: MultipartFile, @RequestParam actionUserId: String, @RequestParam provinceCode: String): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTournamentId: $sportTournamentId, file: ${file.originalFilename}, provinceCode: $provinceCode, actionUserId: $actionUserId")
        try {
            if (!Util.getExtensionByStringHandling(file.originalFilename)?.get().equals("xlsx"))
                return ResponseEntity.badRequest().body(
                    ResponseModel(
                        message = "File type support only xlsx",
                        status = "error",
                        timestamp = LocalDateTime.now(),
                        data = null,
                        pagination = null
                    )
                )
        } catch (e: Exception) {
            logger.error("Wrong format XLSX: $e")
            return ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = "Error format XLSX!!",
                    status = "error",
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }

        return service.importExcel(sportTournamentId, file, actionUserId, false, provinceCode)
    }

    @PostMapping("/excel/import-confirm")
    fun confirmImportExcel(@Valid @RequestParam sportTournamentId: String, @RequestParam("uploadfile") file: MultipartFile, @RequestParam actionUserId: String, @RequestParam provinceCode: String, @RequestParam isConfirm: Boolean): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTournamentId: $sportTournamentId , file: ${file.originalFilename}, provinceCode: $provinceCode, actionUserId: $actionUserId, isConfirm: $isConfirm")
        try {
            if (!Util.getExtensionByStringHandling(file.originalFilename)?.get().equals("xlsx"))
                return ResponseEntity.badRequest().body(
                    ResponseModel(
                        message = "File type support only xlsx",
                        status = "error",
                        timestamp = LocalDateTime.now(),
                        data = null,
                        pagination = null
                    )
                )
        } catch (e: Exception) {
            logger.error("Wrong format XLSX: $e")
            return ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = "Error format XLSX!!",
                    status = "error",
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return service.importExcel(sportTournamentId, file, actionUserId, isConfirm, provinceCode)
    }


    @GetMapping("/excel/download")
    fun downloadExcel(@RequestParam sportTournamentId: String, @RequestParam excelId: String): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTournamentId: $sportTournamentId, excelId: $excelId")
        return service.downloadExcel(sportTournamentId, excelId)
    }

    @GetMapping("/excel/download/template")
    fun downloadExcelTemplate(@RequestParam surveySportId: String): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTournamentId: $surveySportId")
        return service.downloadExcelTemplate(surveySportId)
    }

    @GetMapping("/excel")
    fun getListImport(@Valid @RequestParam sportTournamentId: String): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTournamentId: $sportTournamentId")
        return service.getListImport(sportTournamentId)
    }

    @PostMapping("/excel/compare")
    fun getCompare(@Valid @RequestBody requestModel: RequestCompareCriteria): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("<---begin--->")
        logger.info("request: $requestModel")
        return service.compareTournament(requestModel)
    }

    @GetMapping("/dashboard")
    fun getDashboard(@Valid @RequestParam sportTourId: String, @RequestParam monthDate: String): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("sportTourId: $sportTourId, monthDate: $monthDate")
        val regex = Regex("\\d{4}-\\d{2}")
        if (monthDate.isBlank() || !regex.matches(monthDate))
            throw MissingServletRequestParameterException("monthDate", monthDate)

        if (sportTourId.isBlank())
            throw MissingServletRequestParameterException("surveySportId", sportTourId)

        return service.getDashboardInfo(sportTourId, monthDate)

    }
}