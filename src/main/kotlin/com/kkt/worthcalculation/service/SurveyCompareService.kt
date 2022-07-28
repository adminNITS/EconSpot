package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.db.SportTournamentInfoExcelEntity
import com.kkt.worthcalculation.db.SportTournamentInfoExcelRepository
import com.kkt.worthcalculation.db.SurveySportRepository
import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ResponseCompare
import com.kkt.worthcalculation.model.SurveyCompare
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.RequestCompareCriteria
import com.kkt.worthcalculation.util.ReadImportFileUtil
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@Service
class SurveyCompareService(
    private val sportTourRepo: SportTournamentInfoExcelRepository,
    private val surveySportRepo: SurveySportRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun getListImport(sportTournamentId: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>

        val data = sportTourRepo.findAllBySportTournamentIdOrderByCreateDateDesc(sportTournamentId)
        for (x in data) {
            x.sportTournament = getSportTournament(x.sportTournamentId)
        }

        try {
            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = data,
                    pagination = null
                )
            )
        } catch (e: Exception) {
            logger.error(e.message)
            response = ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = TextConstant.RESP_FAILED_DESC + "|${e.message}",
                    status = TextConstant.RESP_FAILED_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return response

    }

    fun importExcel(sportTournamentId: String, file: MultipartFile, actionUserId: String, isConfirm: Boolean, provinceCode: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            val excelRowData = ReadImportFileUtil.readFromExcelFile(file)
            logger.info("Excel Data: $excelRowData")
            val data = sportTourRepo.findBySportTournamentIdAndExcelLocationAndExcelPeriodDate(sportTournamentId, excelRowData.exTournamentLocation, excelRowData.exTournamentPeriodDate)
            if (!isConfirm) {
                if (data.isNotEmpty()) {
                    logger.info("Confirm duplicate ID: ${data.get(0).id}")
                    response = ResponseEntity.ok(
                        ResponseModel(
                            message = TextConstant.RESP_DUP_DESC,
                            status = TextConstant.RESP_DUP_STATUS,
                            timestamp = LocalDateTime.now(),
                            data = data,
                            pagination = null
                        )
                    )
                } else {
                    logger.info("New Import Excel")
                    sportTourRepo.save(
                        SportTournamentInfoExcelEntity(
                            id = UUID.randomUUID().toString(),
                            sportTournamentId = sportTournamentId,
                            provinceCode = provinceCode,
                            excelFileName = file.originalFilename,
                            excelData = file.bytes,
                            excelContentType = file.contentType,
                            excelLocation = excelRowData.exTournamentLocation,
                            excelPeriodDate = excelRowData.exTournamentPeriodDate,
                            excelBudgetValue = excelRowData.exTournamentBudgetValue,
                            excelNetWorthValue = excelRowData.exTournamentNetWorthValue,
                            excelEconomicValue = excelRowData.exTournamentEconomicValue,
                            excelTotalSpend = excelRowData.exTournamentTotalSpend,
                            createBy = actionUserId,
                            createDate = Date(),
                            updateBy = null,
                            updateDate = null,
                            sportTournament = getSportTournament(sportTournamentId)
                        )
                    )
                    logger.info("record db success!")
                    response = ResponseEntity.ok(
                        ResponseModel(
                            message = TextConstant.RESP_SUCCESS_DESC,
                            status = TextConstant.RESP_SUCCESS_STATUS,
                            timestamp = LocalDateTime.now(),
                            data = null,
                            pagination = null
                        )
                    )
                }
            } else {
                logger.info("Update duplicate ID: ${data.get(0).id}")
                sportTourRepo.save(
                    SportTournamentInfoExcelEntity(
                        id = data.get(0).id,
                        sportTournamentId = sportTournamentId,
                        provinceCode = provinceCode,
                        excelFileName = file.originalFilename,
                        excelData = file.bytes,
                        excelContentType = file.contentType,
                        excelLocation = excelRowData.exTournamentLocation,
                        excelPeriodDate = excelRowData.exTournamentPeriodDate,
                        excelBudgetValue = excelRowData.exTournamentBudgetValue,
                        excelNetWorthValue = excelRowData.exTournamentNetWorthValue,
                        excelEconomicValue = excelRowData.exTournamentEconomicValue,
                        excelTotalSpend = excelRowData.exTournamentTotalSpend,
                        createBy = data.get(0).createBy,
                        createDate = data.get(0).createDate,
                        updateBy = actionUserId,
                        updateDate = Date(),
                        sportTournament = getSportTournament(sportTournamentId)
                    )
                )
                logger.info("record db success!")
                response = ResponseEntity.ok(
                    ResponseModel(
                        message = TextConstant.RESP_SUCCESS_DESC,
                        status = TextConstant.RESP_SUCCESS_STATUS,
                        timestamp = LocalDateTime.now(),
                        data = null,
                        pagination = null
                    )
                )
            }


        } catch (importExcel: ImportExcelException) {
            response = ResponseEntity.badRequest().body(
                ResponseModel(
                    message = TextConstant.RESP_FAILED_DESC + "|${importExcel.message}",
                    status = TextConstant.RESP_FAILED_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        } catch (exception: Exception) {
            response = ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = TextConstant.RESP_FAILED_DESC + "|${exception.message}",
                    status = TextConstant.RESP_FAILED_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }

        return response
    }

    fun downloadExcel(sportTournamentId: String, excelId: String): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val data = sportTourRepo.findById(excelId)
            if (!data.isPresent) throw Exception("Excel not found!!")
            response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(data.get().excelContentType.toString()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + data.get().excelFileName + "\"")
                .body(data.get().excelData);
        } catch (e: Exception) {
            logger.error(e.message)
            response = ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = TextConstant.RESP_FAILED_DESC + "|${e.message}",
                    status = TextConstant.RESP_FAILED_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return response
    }

    fun getDashboardInfo(surveySportId: String, monthDate: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            val formatterRequest = SimpleDateFormat("yyyy-MM")
            val formatterA = SimpleDateFormat("yyyy-MM-dd")
            val mDate = formatterRequest.parse(monthDate)

            val ss: LocalDate = LocalDate.parse(formatterA.format(mDate))
            val startDayOfMonth: LocalDate = ss.withDayOfMonth(1)
            val endDayOfMonth: LocalDate = ss.withDayOfMonth(ss.lengthOfMonth())

            logger.info(startDayOfMonth.toString())
            logger.info(endDayOfMonth.toString())
            val listSurveySport = surveySportRepo.findAllBySurveySportIdAndAndStartDateBetween(surveySportId, formatterA.parse(startDayOfMonth.toString()), formatterA.parse(endDayOfMonth.toString()));
            if (listSurveySport.isNotEmpty()) {
                for (x in listSurveySport) {
                    x.sportTournament = getSportTournament(x.sportTourId.toString())
                    x.sportTournamentSurveyExcel = sportTourRepo.findAllBySportTournamentIdOrderByCreateDateDesc(x.sportTourId.toString())
                }
            }
            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = listSurveySport,
                    pagination = null
                )
            )
        } catch (e: Exception) {
            logger.error(e.message)
            response = ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = TextConstant.RESP_FAILED_DESC + "|${e.message}",
                    status = TextConstant.RESP_FAILED_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return response
    }

    fun compareTournament(req: RequestCompareCriteria): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>



        try {
            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = ResponseCompare(
                        tournamentA = SurveyCompare(
                            svCompareId = "1",
                            sportTournamentId = req.tournamentA.tournamentId,
                            sportTournament = getSportTournament(req.tournamentA.tournamentId),
                            svCompareExcelFileName = "test.xlsx",
                            svCompareExcelSourceData = "binary Text",
                            svCompareBudgetValue = "2000000",
                            svCompareNetWorthValue = "2500000",
                            svCompareEconomicValue = "1900000"
                        ),
                        tournamentB = SurveyCompare(
                            svCompareId = "1",
                            sportTournamentId = req.tournamentB.tournamentId,
                            sportTournament = getSportTournament(req.tournamentA.tournamentId),
                            svCompareExcelFileName = "test.xlsx",
                            svCompareExcelSourceData = "binary Text",
                            svCompareBudgetValue = "2000000",
                            svCompareNetWorthValue = "2500000",
                            svCompareEconomicValue = "1900000"
                        )
                    ),
                    pagination = null
                )
            )
        } catch (e: Exception) {
            logger.error(e.message)
            response = ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = TextConstant.RESP_FAILED_DESC + "|${e.message}",
                    status = TextConstant.RESP_FAILED_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return response
    }

    private fun getSportTournament(sportTournamentId: String): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("http://34.143.176.77:4567/rest/sportTournament/$sportTournamentId", Any::class.java).body as Map<*, *>
        return response["data"]
    }
}

