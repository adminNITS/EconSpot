package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.config.ConfigProperties
import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.db.SportTournamentInfoExcelEntity
import com.kkt.worthcalculation.db.SportTournamentInfoExcelRepository
import com.kkt.worthcalculation.db.SurveySportRepository
import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ResponseCompare
import com.kkt.worthcalculation.model.User
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.RequestCompareCriteria
import com.kkt.worthcalculation.model.criteria.Tournament
import com.kkt.worthcalculation.util.Util
import com.kkt.worthcalculation.util.Util.Companion.writeExcelFile
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.domain.Specification
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Root


@Service
class SurveyCompareService(
    private val sportTourRepo: SportTournamentInfoExcelRepository,
    private val surveySportRepo: SurveySportRepository,
    private val properties: ConfigProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun getListImport(surveySportId: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        val data = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(surveySportId)
        logger.info("Found Data: ${data.size}")
        for (x in data) {
//            x.sportTournament = getSportTournament(x.sportTournamentId)
            x.excelData = null
            x.user = x.createBy?.let { getUser(it) }
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

    fun importExcel(surveySportId: String, file: MultipartFile, actionUserId: String, isConfirm: Boolean, provinceCode: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            val excelRowData = Util.readFromExcelFile(file)
            logger.info("Excel Data: $excelRowData")
            val data = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(surveySportId)
            if (!isConfirm) {
                if (data.isNotEmpty()) {
                    data[0].excelData = null
                    logger.info("Confirm duplicate ID: ${data[0].id}")
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
                            surveySportId = surveySportId,
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
                            excelSportProject = excelRowData.exTournamentName,
                            createBy = actionUserId,
                            createDate = Date(),
                            updateBy = null,
                            updateDate = null,
                            sportTournament = null,
                            user = null
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
                logger.info("Update duplicate ID: ${data[0].id}")
                sportTourRepo.save(
                    SportTournamentInfoExcelEntity(
                        id = UUID.randomUUID().toString(),
                        surveySportId = surveySportId,
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
                        excelSportProject = excelRowData.exTournamentName,
                        createBy = data[0].createBy,
                        createDate = data[0].createDate,
                        updateBy = actionUserId,
                        updateDate = Date(),
                        sportTournament = null,
                        user = null
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

    fun downloadExcel(surveySportId: String, excelId: String): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val data = sportTourRepo.findById(excelId)
            if (!data.isPresent) throw Exception("Excel not found!!")
            response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(data.get().excelContentType.toString()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + data.get().excelFileName + "\"")
                .body(data.get().excelData)
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

    fun downloadExcelTemplate(surveySportId: String): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        println(properties.excelReportA)
        try {
            val data = getSurveySport(surveySportId) as Map<*, *>
            if (data.isNotEmpty()) {
                val sportTour = data["sportTour"] as Map<*, *>
                val province = data["province"] as Map<*, *>
                val sportTournamentName: String = sportTour["sportTourName"].toString()
                val location: String = province["provinceName"].toString()
                val startDate: String = convertDateFormatTH(data["startDate"].toString())
                val endDate: String = convertDateFormatTH(data["endDate"].toString())
                val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelImportMaster.toByteArray()))
                val excelData = writeExcelFile(fileImportMaster, sportTournamentName, location, startDate, endDate)

                response = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ImportMaster.xlsx")
                    .body(excelData)
            } else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseModel(
                        message = TextConstant.RESP_NOT_FOUND_DESC,
                        status = TextConstant.RESP_NOT_FOUND_STATUS,
                        timestamp = LocalDateTime.now(),
                        data = surveySportId,
                        pagination = null
                    )
                )
            }

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

    fun getDashboardInfo(sportTourId: String, monthDate: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            val formatterRequest = SimpleDateFormat("yyyy-MM")
            val formatterA = SimpleDateFormat("yyyy-MM-dd")
            val mDate = formatterRequest.parse(monthDate)

            val ss: LocalDate = LocalDate.parse(formatterA.format(mDate))
            val startDayOfMonth: LocalDate = ss.withDayOfMonth(1)
            val endDayOfMonth: LocalDate = ss.withDayOfMonth(ss.lengthOfMonth())

            logger.info("Search Date Range: $startDayOfMonth - $endDayOfMonth")
            val listSurveySport = surveySportRepo.findAllBySportTourIdAndStartDateBetween(sportTourId, formatterA.parse(startDayOfMonth.toString()), formatterA.parse(endDayOfMonth.toString()))
            logger.info("Found Data: ${listSurveySport.size}")
            if (listSurveySport.isNotEmpty()) {
                for (x in listSurveySport) {
                    x.sportTournament = getSportTournament(x.sportTourId.toString())
                    val listSportTour = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(x.surveySportId.toString())
                    if (listSportTour.isNotEmpty()) {
                        x.sportTournamentSurveyExcel = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(x.surveySportId.toString())[0]
                        (x.sportTournamentSurveyExcel as SportTournamentInfoExcelEntity).excelData = null
                        (x.sportTournamentSurveyExcel as SportTournamentInfoExcelEntity).user = (x.sportTournamentSurveyExcel as SportTournamentInfoExcelEntity).createBy?.let { getUser(it) }
                    }
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
            val tournamentA = sportTourRepo.findAll(genWhere(req.tournamentA))
            val tournamentB = sportTourRepo.findAll(genWhere(req.tournamentB))
            logger.info("TournamentA Size: ${tournamentA.size}, TournamentB Size: ${tournamentB.size}")
            if (tournamentA.isNotEmpty()) {
//                tournamentA[0].sportTournament = getSportTournament(tournamentA[0].sportTournamentId)
                tournamentA[0].excelData = null
                tournamentA[0].user = tournamentA[0].createBy?.let { getUser(it) }
            }

            if (tournamentB.isNotEmpty()) {
//                tournamentB[0].sportTournament = getSportTournament(tournamentB[0].sportTournamentId)
                tournamentB[0].excelData = null
                tournamentB[0].user = tournamentB[0].createBy?.let { getUser(it) }
            }

            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = ResponseCompare(
                        tournamentA = tournamentA,
                        tournamentB = tournamentB
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

    private fun getSurveySport(surveySportId: String): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("http://34.143.176.77:4567/rest/surveySport/$surveySportId", Any::class.java).body as Map<*, *>
        return response["data"]
    }

    private fun getUser(userId: String): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("http://34.143.176.77:4567/rest/user/$userId", Any::class.java).body as Map<*, *>
        val ss = response["data"] as Map<*, *>
        return User(
            fname = ss["fname"].toString(),
            lname = ss["lname"].toString(),
            email = ss["email"].toString()
        )
    }

    private fun genWhere(objC: Tournament): Specification<SportTournamentInfoExcelEntity> {
        return Specification<SportTournamentInfoExcelEntity> { sp: Root<SportTournamentInfoExcelEntity?>, _: CriteriaQuery<*>?, cb: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList<Predicate>()
            if (objC.sportTourId.isNotBlank())
                predicates.add(cb.equal(sp.get<Any>("sportTournamentId"), objC.sportTourId))

            if (objC.provinceCode?.isNotBlank() == true)
                predicates.add(cb.equal(sp.get<Any>("provinceCode"), objC.provinceCode))

            if (objC.location?.isNotBlank() == true)
                predicates.add(cb.equal(sp.get<Any>("excelLocation"), objC.location))

            if (objC.sportProject?.isNotBlank() == true)
                predicates.add(cb.equal(sp.get<Any>("excelSportProject"), objC.sportProject))

            cb.and(*predicates.toTypedArray())
        }
    }

    private fun convertDateFormatTH(dateStr: String): String {
        val sdf1 = SimpleDateFormat("yyyy-MM-dd")
        val sdf2 = SimpleDateFormat("dd/MMM/yyyy", Locale("th", "th"))
        val date = sdf1.parse(dateStr)
        return sdf2.format(date)
    }

}

