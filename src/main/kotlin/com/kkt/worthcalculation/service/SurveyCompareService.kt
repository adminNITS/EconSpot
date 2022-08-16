package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.config.ConfigProperties
import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.db.*
import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ExcelData
import com.kkt.worthcalculation.model.GenerateExcelData
import com.kkt.worthcalculation.model.User
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.RequestCompareCriteria
import com.kkt.worthcalculation.model.criteria.SurveySport
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
import java.net.URLEncoder
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
    private val properties: ConfigProperties,
    private val irrMasterRepository: IrrMasterRepository
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
//                            excelNetWorthValue = excelRowData.exTournamentNetWorthValue,
                            excelNetWorthValue = calNetWorth(excelRowData.exTournamentBudgetValue, excelRowData.exTournamentEconomicValue),
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
            val filename: String = URLEncoder.encode(data.get().excelFileName, "UTF-8")
            response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(data.get().excelContentType.toString()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
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
        try {
            val data = getSurveySport(surveySportId) as Map<*, *>
            val answerData = getAnswer(surveySportId) as ArrayList<Map<*, *>>
            var dataExcel: GenerateExcelData? = null

            if (answerData.isNotEmpty()) {
                val s01 = answerData.filter { it -> it["surveyMasterId"].toString() == "S01" }
                val s06 = answerData.filter { it -> it["surveyMasterId"].toString() == "S06" }
                val s02 = answerData.filter { it -> it["surveyMasterId"].toString() == "S02" }
                val s03 = answerData.filter { it -> it["surveyMasterId"].toString() == "S03" }
                val s04 = answerData.filter { it -> it["surveyMasterId"].toString() == "S04" }

                logger.info("S01 size: " + s01.size)
                logger.info("S03 size: " + s03.size)
                logger.info("S02 size: " + s02.size)
                logger.info("S04 size: " + s04.size)
                logger.info("S06 size: " + s06.size)

                //Begin Get Value from S01
                var competitionValueFromS01 = 0
                var broadcastsS01Value = 0
                var kktS01Value = 0
                var tttS01Value = 0
                var sectorS01Value = 0
                var licenceS01Value = 0
                var ticketS01Value = 0
                var otherS01Value = 0

                var playerTHNumberS01 = 0
                var playerENNumberS01 = 0
                var coachTHNumberS01 = 0
                var coachENNumberS01 = 0
                var followerTHNumberS01 = 0
                var followerENNumberS01 = 0
                var newsTHNumberS01 = 0
                var newsENNumberS01 = 0
                var watcherTHNumberS01 = 0
                var watcherENNumberS01 = 0

                for (index in s01.indices) {
                    val answers = s01[index]["answers"] as ArrayList<Map<*, *>>
                    val q102003: ArrayList<Map<*, *>>
                    val q102004: ArrayList<Map<*, *>>
                    val q102009: ArrayList<Map<*, *>>
                    if (answers.isNotEmpty()) {
                        q102003 = answers.filter { it -> it["questionId"].toString() == "Q0102003" } as ArrayList<Map<*, *>>
                        q102004 = answers.filter { it -> it["questionId"].toString() == "Q0102004" } as ArrayList<Map<*, *>>
                        q102009 = answers.filter { it -> it["questionId"].toString() == "Q0102009" } as ArrayList<Map<*, *>>
                        if (q102003.isNotEmpty()) {
                            competitionValueFromS01 += getValue(q102003, "anwser1")
                            tttS01Value += getValue(q102003, "anwser2")
                            kktS01Value += getValue(q102003, "anwser3")
                            sectorS01Value += getValue(q102003, "anwser5")
                            ticketS01Value += getValue(q102003, "anwser7")
                            otherS01Value += getValue(q102003, "anwser9")
                            licenceS01Value += getValue(q102003, "anwser10")
                        }

                        if (q102009.isNotEmpty())
                            broadcastsS01Value += getValue(q102009, "anwser3")

                        if (q102004.isNotEmpty()) {
                            playerTHNumberS01 += getValue(q102004, "anwser2")
                            playerENNumberS01 += getValue(q102004, "anwser3")
                            coachTHNumberS01 += getValue(q102004, "anwser4")
                            coachENNumberS01 += getValue(q102004, "anwser5")
                            followerTHNumberS01 += getValue(q102004, "anwser6")
                            followerENNumberS01 += getValue(q102004, "anwser7")
                            newsTHNumberS01 += getValue(q102004, "anwser10")
                            newsENNumberS01 += getValue(q102004, "anwser11")
                            watcherTHNumberS01 += getValue(q102004, "anwser8")
                            watcherENNumberS01 += getValue(q102004, "anwser9")
                        }

                    }

                }

                var competitionValueFromS06 = 0
                var broadcastsS06Value = 0
                var kktS06Value = 0
                var tttS06Value = 0
                var sectorS06Value = 0
                var licenceS06Value = 0
                var ticketS06Value = 0
                var otherS06Value = 0

                var playerTHNumberS06 = 0
                var playerENNumberS06 = 0
                var coachTHNumberS06 = 0
                var coachENNumberS06 = 0
                var followerTHNumberS06 = 0
                var followerENNumberS06 = 0
                var newsTHNumberS06 = 0
                var newsENNumberS06 = 0
                var watcherTHNumberS06 = 0
                var watcherENNumberS06 = 0
                for (index in s06.indices) {
                    val answers = s06[index]["answers"] as ArrayList<Map<*, *>>
                    val q602003: ArrayList<Map<*, *>>
                    val q602009: ArrayList<Map<*, *>>
                    val q602004: ArrayList<Map<*, *>>
                    if (answers.isNotEmpty()) {
                        q602003 = answers.filter { it -> it["questionId"].toString() == "Q0602003" } as ArrayList<Map<*, *>>
                        if (q602003.isNotEmpty()) {
                            competitionValueFromS06 += getValue(q602003, "anwser1")
                            tttS06Value += getValue(q602003, "anwser2")
                            kktS06Value += getValue(q602003, "anwser3")
                            sectorS06Value += getValue(q602003, "anwser5")
                            ticketS06Value += getValue(q602003, "anwser7")
                            otherS06Value += getValue(q602003, "anwser9")
                            licenceS06Value += getValue(q602003, "anwser10")
                        }

                        q602009 = answers.filter { it -> it["questionId"].toString() == "Q0602009" } as ArrayList<Map<*, *>>
                        if (q602009.isNotEmpty())
                            broadcastsS06Value += getValue(q602009, "anwser3")

                        q602004 = answers.filter { it -> it["questionId"].toString() == "Q0602004" } as ArrayList<Map<*, *>>
                        if (q602004.isNotEmpty()) {
                            playerTHNumberS06 += getValue(q602004, "anwser2")
                            playerENNumberS06 += getValue(q602004, "anwser3")
                            coachTHNumberS06 += getValue(q602004, "anwser4")
                            coachENNumberS06 += getValue(q602004, "anwser5")
                            followerTHNumberS06 += getValue(q602004, "anwser6")
                            followerENNumberS06 += getValue(q602004, "anwser7")
                            newsTHNumberS06 += getValue(q602004, "anwser10")
                            newsENNumberS06 += getValue(q602004, "anwser11")
                            watcherTHNumberS06 += getValue(q602004, "anwser8")
                            watcherENNumberS06 += getValue(q602004, "anwser9")
                        }
                    }
                }

                var totalSpendPlayerTH = 0
                for (index in s02.indices) {
                    var sumOfPlayer = 0
                    val answers = s02[index]["answers"] as ArrayList<Map<*, *>>
                    val q202005: ArrayList<Map<*, *>>
                    val q202006: ArrayList<Map<*, *>>
                    if (answers.isNotEmpty()) {
                        q202005 = answers.filter { it -> it["questionId"].toString() == "Q0202005" } as ArrayList<Map<*, *>>
                        if (q202005.isNotEmpty()) {
                            sumOfPlayer += getValue(q202005, "anwser2")
                            sumOfPlayer += getValue(q202005, "anwser6")
                            sumOfPlayer += getValue(q202005, "anwser10")
                            sumOfPlayer += getValue(q202005, "anwser14")
                            sumOfPlayer += getValue(q202005, "anwser19")
                        }

                        q202006 = answers.filter { it -> it["questionId"].toString() == "Q0202006" } as ArrayList<Map<*, *>>
                        if (q202006.isNotEmpty()) {
                            sumOfPlayer += getValue(q202006, "anwser2")
                            sumOfPlayer += getValue(q202006, "anwser6")
                            sumOfPlayer += getValue(q202006, "anwser10")
                            sumOfPlayer += getValue(q202006, "anwser14")
                            sumOfPlayer += getValue(q202006, "anwser18")
                            sumOfPlayer += getValue(q202006, "anwser22")
                            sumOfPlayer += getValue(q202006, "anwser26")
                            sumOfPlayer += getValue(q202006, "anwser30")
                            sumOfPlayer += getValue(q202006, "anwser34")
                            sumOfPlayer += getValue(q202006, "anwser39")
                        }
                    }
                    totalSpendPlayerTH += sumOfPlayer
                }

                var totalSpendCoachTH = 0
                for (index in s03.indices) {
                    var sumOfPlayer = 0
                    val answers = s03[index]["answers"] as ArrayList<Map<*, *>>
                    val q302005: ArrayList<Map<*, *>>
                    val q302006: ArrayList<Map<*, *>>
                    if (answers.isNotEmpty()) {
                        q302005 = answers.filter { it -> it["questionId"].toString() == "Q0302005" } as ArrayList<Map<*, *>>
                        if (q302005.isNotEmpty()) {
                            sumOfPlayer += getValue(q302005, "anwser2")
                            sumOfPlayer += getValue(q302005, "anwser6")
                            sumOfPlayer += getValue(q302005, "anwser10")
                            sumOfPlayer += getValue(q302005, "anwser14")
                            sumOfPlayer += getValue(q302005, "anwser19")
                        }

                        q302006 = answers.filter { it -> it["questionId"].toString() == "Q0302006" } as ArrayList<Map<*, *>>
                        if (q302006.isNotEmpty()) {
                            sumOfPlayer += getValue(q302006, "anwser2")
                            sumOfPlayer += getValue(q302006, "anwser6")
                            sumOfPlayer += getValue(q302006, "anwser10")
                            sumOfPlayer += getValue(q302006, "anwser14")
                            sumOfPlayer += getValue(q302006, "anwser18")
                            sumOfPlayer += getValue(q302006, "anwser22")
                            sumOfPlayer += getValue(q302006, "anwser26")
                            sumOfPlayer += getValue(q302006, "anwser30")
                            sumOfPlayer += getValue(q302006, "anwser34")
                            sumOfPlayer += getValue(q302006, "anwser39")
                        }
                    }
                    totalSpendCoachTH += sumOfPlayer
                }

                var totalSpendWatcherTH = 0
                for (index in s04.indices) {
                    var sum = 0
                    val answers = s04[index]["answers"] as ArrayList<Map<*, *>>
                    val q402008: ArrayList<Map<*, *>>
                    if (answers.isNotEmpty()) {
                        q402008 = answers.filter { it -> it["questionId"].toString() == "Q0402008" } as ArrayList<Map<*, *>>
                        if (q402008.isNotEmpty()) {
                            sum += getValue(q402008, "anwser2")
                            sum += getValue(q402008, "anwser3")
                            sum += getValue(q402008, "anwser5")
                            sum += getValue(q402008, "anwser6")
                            sum += getValue(q402008, "anwser8")
                            sum += getValue(q402008, "anwser9")
                            sum += getValue(q402008, "anwser11")
                            sum += getValue(q402008, "anwser12")
                            sum += getValue(q402008, "anwser14")
                            sum += getValue(q402008, "anwser15")
                            sum += getValue(q402008, "anwser17")
                            sum += getValue(q402008, "anwser18")
                            sum += getValue(q402008, "anwser20")
                            sum += getValue(q402008, "anwser21")
                            sum += getValue(q402008, "anwser23")
                            sum += getValue(q402008, "anwser24")
                            sum += getValue(q402008, "anwser26")
                            sum += getValue(q402008, "anwser27")
                            sum += getValue(q402008, "anwser29")
                            sum += getValue(q402008, "anwser30")
                        }
                    }
                    totalSpendWatcherTH += sum
                }


                // End Get Value from S06
                dataExcel = GenerateExcelData(
                    f5 = (competitionValueFromS01 + competitionValueFromS06) / 2,
                    g5 = (broadcastsS01Value + broadcastsS06Value) / 2,
                    k5 = (kktS01Value + kktS06Value) / 2,
                    l5 = (tttS01Value + tttS06Value) / 2,
                    m5 = (broadcastsS01Value + broadcastsS06Value) / 2,
                    n5 = (sectorS01Value + sectorS06Value) / 2,
                    o5 = (ticketS01Value + ticketS06Value) / 2,
                    p5 = (otherS01Value + otherS06Value) / 2,
                    s5 = (playerTHNumberS01 + playerTHNumberS06) / 2,
                    t5 = (playerENNumberS01 + playerENNumberS06) / 2,
                    u5 = (coachTHNumberS01 + coachTHNumberS06) / 2,
                    v5 = (coachENNumberS01 + coachENNumberS06) / 2,
                    w5 = (followerTHNumberS01 + followerTHNumberS06) / 2,
                    x5 = (followerENNumberS01 + followerENNumberS06) / 2,
                    y5 = (newsTHNumberS01 + newsTHNumberS01) / 2,
                    z5 = (newsENNumberS01 + newsENNumberS01) / 2,
                    ac5 = (watcherTHNumberS01 + watcherTHNumberS06) / 2,
                    ad5 = (watcherENNumberS01 + watcherENNumberS06) / 2,
                    ae5 = totalSpendPlayerTH,
                    ag5 = totalSpendCoachTH,
                    ao5 = totalSpendWatcherTH,
                    aq5 = totalSpendWatcherTH + totalSpendCoachTH + totalSpendPlayerTH
                )

                logger.info("ค่าจัดการแข่งขัน = " + (competitionValueFromS01 + competitionValueFromS06) / 2)
                logger.info("การประชาสัมพันธ์และถ่ายทอดสด = " + (broadcastsS01Value + broadcastsS06Value) / 2)
                logger.info("K5 กกท = " + (kktS01Value + kktS06Value) / 2)
                logger.info("L5 ททท = " + (tttS01Value + tttS06Value) / 2)
                logger.info("M5 ค่าถ่ายทอด = " + (broadcastsS01Value + broadcastsS06Value) / 2)
                logger.info("N5 เอกชน = " + (sectorS01Value + sectorS06Value) / 2)
                logger.info("O5 ขายบัตร = " + (ticketS01Value + ticketS06Value) / 2)
                logger.info("P5 อื่นๆ = " + (otherS01Value + otherS06Value) / 2)

                logger.info("S5 นักกีฬา TH = " + (playerTHNumberS01 + playerTHNumberS06) / 2)
                logger.info("T5 นักกีฬา EN = " + (playerENNumberS01 + playerENNumberS06) / 2)
                logger.info("U5 ผู้ฝึกสอน TH = " + (coachTHNumberS01 + coachTHNumberS06) / 2)
                logger.info("V5 ผู้ฝึกสอน EN = " + (coachENNumberS01 + coachENNumberS06) / 2)
                logger.info("W5 ผู้ติดตาม TH = " + (followerTHNumberS01 + followerTHNumberS06) / 2)
                logger.info("X5 ผู้ติดตาม EN = " + (followerENNumberS01 + followerENNumberS06) / 2)
                logger.info("Y5 นักข่าว TH = " + (newsTHNumberS01 + newsTHNumberS01) / 2)
                logger.info("Z5 นักข่าว EN = " + (newsENNumberS01 + newsENNumberS01) / 2)
                logger.info("AC5 ผู้ชม TH = " + (watcherTHNumberS01 + watcherTHNumberS06) / 2)
                logger.info("AD5 ผู้ชม EN = " + (watcherENNumberS01 + watcherENNumberS06) / 2)
                logger.info("AE5 ค่าใช้จ่ายเฉลี่ย นักกีฬา TH = $totalSpendPlayerTH")
                logger.info("AG5 ค่าใช้จ่ายเฉลี่ย ผู้ฝึกสอน TH = $totalSpendCoachTH")
                logger.info("AO5 ค่าใช้จ่ายเฉลี่ย ผู้ชม TH = $totalSpendWatcherTH")
                logger.info("AQ5 มูลค่าการใช้จ่าย = ${totalSpendWatcherTH + totalSpendCoachTH + totalSpendPlayerTH}")

            }

            if (data.isNotEmpty()) {
                val sportTour = data["sportTour"] as Map<*, *>
                val province = data["province"] as Map<*, *>
                val sportTournamentName: String = sportTour["sportTourName"].toString()
                val location: String = province["provinceName"].toString()
                val startDate: String = Util.convertDateFormatTH(data["startDate"].toString())
                val endDate: String = Util.convertDateFormatTH(data["endDate"].toString())
                if (dataExcel != null) {
                    dataExcel.b5 = sportTournamentName
                    dataExcel.c5 = location
                    dataExcel.d5 = "$startDate - $endDate"
                }
                val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelImportMaster.toByteArray()))
                var excelData: ByteArray? = null
                if (dataExcel != null) {
                    dataExcel.b5 = sportTournamentName
                    dataExcel.c5 = location
                    dataExcel.d5 = "$startDate - $endDate"
                    excelData = writeExcelFile(fileImportMaster, dataExcel)
                }
                val filename: String = URLEncoder.encode("template-$sportTournamentName-$location-$startDate-$endDate.xlsx", "UTF-8")
                response = ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(TextConstant.EXCEL_SHEET_CONTENT_TYPE))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
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

    private fun getValue(data: ArrayList<Map<*, *>>, key: String): Int {
        try {
            if (data[0][key]?.toString() != null) {
                return data[0][key].toString().toInt()
            } else {
                return 0
            }
        } catch (e: Exception) {
            logger.error("error $key can't parse to Int" + e.message)
            return 0
        }

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
                    val sportTournament = getSportTournament(x.sportTourId.toString()) as Map<*, *>
                    val listSportTour = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(x.surveySportId.toString())
                    if (listSportTour.isNotEmpty()) {
                        val sportTournamentSurveyExcel = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(x.surveySportId.toString())[0]
                        x.sportTournamentSurveyExcel = ExcelData(
                            excelBudgetValue = sportTournamentSurveyExcel.excelBudgetValue,
                            excelEconomicValue = sportTournamentSurveyExcel.excelEconomicValue,
                            excelNetWorthValue = sportTournamentSurveyExcel.excelNetWorthValue,
                            excelTotalSpend = sportTournamentSurveyExcel.excelTotalSpend,
                            tournamentName = sportTournament["sportTourName"].toString()
                        )
//                        (x.sportTournamentSurveyExcel as SportTournamentInfoExcelEntity).user = (x.sportTournamentSurveyExcel as SportTournamentInfoExcelEntity).createBy?.let { getUser(it) }
                    } else {
                        x.sportTournamentSurveyExcel = ExcelData(
                            excelBudgetValue = "0",
                            excelEconomicValue = "0",
                            excelNetWorthValue = "0",
                            excelTotalSpend = "0",
                            tournamentName = sportTournament["sportTourName"].toString()
                        )
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
            val surveySportData = surveySportRepo.findAll(genWhere(req.surveySport))
            logger.info("surveySportData Size: ${surveySportData.size}")
            if (surveySportData.isNotEmpty()) {
                val sportTournamentExcel = sportTourRepo.findAllBySurveySportIdOrderByCreateDateDesc(surveySportData[0].surveySportId.toString())
                logger.info("sportTournamentExcel Size: ${sportTournamentExcel.size}")
                if (sportTournamentExcel.isNotEmpty()) {
                    sportTournamentExcel[0].excelData = null
                    sportTournamentExcel[0].user = sportTournamentExcel[0].createBy?.let { getUser(it) }

                    response = ResponseEntity.ok(
                        ResponseModel(
                            message = TextConstant.RESP_SUCCESS_DESC,
                            status = TextConstant.RESP_SUCCESS_STATUS,
                            timestamp = LocalDateTime.now(),
                            data = sportTournamentExcel[0],
                            pagination = null
                        )
                    )
                } else {
                    response = ResponseEntity.ok(
                        ResponseModel(
                            message = TextConstant.RESP_NOT_FOUND_DESC,
                            status = TextConstant.RESP_NOT_FOUND_STATUS,
                            timestamp = LocalDateTime.now(),
                            data = null,
                            pagination = null
                        )
                    )
                }


            } else {
                response = ResponseEntity.ok(
                    ResponseModel(
                        message = TextConstant.RESP_NOT_FOUND_DESC,
                        status = TextConstant.RESP_NOT_FOUND_STATUS,
                        timestamp = LocalDateTime.now(),
                        data = null,
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

    private fun getSportTournament(sportTournamentId: String): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/sportTournament/$sportTournamentId", Any::class.java).body as Map<*, *>
        return response["data"]
    }

    private fun getSurveySport(surveySportId: String): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/surveySport/$surveySportId", Any::class.java).body as Map<*, *>
        return response["data"]
    }

    private fun getUser(userId: String): Any {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/user/$userId", Any::class.java).body as Map<*, *>
        val ss = response["data"] as Map<*, *>
        return User(
            fname = ss["fname"].toString(),
            lname = ss["lname"].toString(),
            email = ss["email"].toString()
        )
    }

    private fun getAnswer(surveySportId: String): Any? {
        val restTemplate = RestTemplate()
        return restTemplate.getForEntity("${properties.existingHostMongo}/searchSurveyAnswers?surveySportId=$surveySportId", Any::class.java).body
    }

    private fun calNetWorth(budgetValue: String?, economicValue: String?): String {
        return try {
            val d = irrMasterRepository.searchTopByEffectiveDateBeforeOrderByEffectiveDateDesc(Date())
            val irr1 = 1 + (d.interest.toDouble())
            val ss = budgetValue?.toDouble()?.let { economicValue?.toDouble()?.minus(it) }
            val r = String.format("%.2f", ss?.div(irr1))
            logger.info("Bt: $economicValue")
            logger.info("Ct: $budgetValue")
            logger.info("Bt - Ct = $ss")
            logger.info("1 + irr = $irr1")
            logger.info("(Bt - Ct)/1 + irr = $r")
            r
        } catch (e: Exception) {
            logger.error(e.message)
            "0"
        }

    }

    private fun genWhere(objC: SurveySport): Specification<SurveySportEntity> {
        return Specification<SurveySportEntity> { sp: Root<SurveySportEntity?>, _: CriteriaQuery<*>?, cb: CriteriaBuilder ->
            val predicates: MutableList<Predicate> = ArrayList<Predicate>()
            if (objC.sportTourId.isNotBlank())
                predicates.add(cb.equal(sp.get<Any>("sportTourId"), objC.sportTourId))

            if (objC.provinceCode?.isNotBlank() == true)
                predicates.add(cb.equal(sp.get<Any>("provinceCode"), objC.provinceCode))

            if (objC.location?.isNotBlank() == true)
                predicates.add(cb.equal(sp.get<Any>("location"), objC.location))

            if (objC.sportProject?.isNotBlank() == true)
                predicates.add(cb.equal(sp.get<Any>("sportProject"), objC.sportProject))

            cb.and(*predicates.toTypedArray())
        }
    }

}

