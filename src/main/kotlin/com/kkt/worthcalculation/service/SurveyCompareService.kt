package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.model.SurveyCompare
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.RequestCompareCriteria
import org.apache.tomcat.util.http.fileupload.FileItem
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*


@Service
class SurveyCompareService() {

    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun getListImport(sportTournamentId: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            val listData = mutableListOf<SurveyCompare>()
            listData.add(
                SurveyCompare(
                    svCompareId = "1",
                    sportTournamentId = sportTournamentId,
                    svCompareExcelFileName = "test.xlsx",
                    svCompareExcelSourceData = "binary Text",
                    svCompareBudgetValue = "2000000",
                    svCompareNetWorthValue = "2500000",
                    svCompareEconomicValue = "1900000",
                    sportTournament = getSportTournament(sportTournamentId),
                )
            )
            listData.add(
                SurveyCompare(
                    svCompareId = "2",
                    sportTournamentId = sportTournamentId,
                    svCompareExcelFileName = "test.xlsx",
                    svCompareExcelSourceData = "binary Text",
                    svCompareBudgetValue = "2100000",
                    svCompareNetWorthValue = "2300000",
                    svCompareEconomicValue = "1700000",
                    sportTournament = getSportTournament(sportTournamentId),
                )
            )

            response = ResponseEntity.ok(
                ResponseModel(
                    message = "success",
                    status = "ok",
                    timestamp = LocalDateTime.now(),
                    data = listData,
                    pagination = null
                )
            )
        } catch (e: Exception) {
            logger.error(e.message)
            response = ResponseEntity.internalServerError().body(
                ResponseModel(
                    message = "${e.message}",
                    status = "error",
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return response

    }

    fun processExcel(sportTournamentId: String, file: MultipartFile): Boolean {
        val t = Base64.getEncoder().encodeToString(file.bytes)
//        logger.info(t.toString())
//
//        val aa: ByteArray? = Base64.getDecoder().decode(t)
//        val inputStream: InputStream = ByteArrayInputStream(aa)
//        val fileItem: FileItem = DiskFileItem("fileData", file.contentType, true, file.originalFilename, 100000000, File(System.getProperty("java.io.tmpdir")))
//        val multipartFile: MultipartFile = CommonsMultipartFile(fileItem)
        return true
    }

    fun compareTournament(req: RequestCompareCriteria): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            response = ResponseEntity.ok(
                ResponseModel(
                    message = "success",
                    status = "ok",
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
                    message = "${e.message}",
                    status = "error",
                    timestamp = LocalDateTime.now(),
                    data = null,
                    pagination = null
                )
            )
        }
        return response
    }
    private fun getSportTournament(sportTournamentId: String): Any? {
        val restTemplate: RestTemplate = RestTemplate()
        val response = restTemplate.getForEntity("http://34.87.106.181:4567/rest/sportTournament/$sportTournamentId", Any::class.java).body as Map<*, *>
        return response["data"]
    }
}

data class ResponseCompare(
    val tournamentA: SurveyCompare,
    val tournamentB: SurveyCompare
)

