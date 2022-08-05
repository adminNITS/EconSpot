package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.config.ConfigProperties
import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.util.Util
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.*

@Service
class ReportService(
    private val properties: ConfigProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun downloadReportGeneral(): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val data = getSurveySport() ?: throw Exception("Excel not found!!")
//            val headers = arrayOf("No.", "Type of activity", "Type of sport", "Activity name", "Create date", "Create by", "Update date", "Update by")
//            val sheetName = "ข้อมูลพื้นฐาน"

            val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelReportA.toByteArray()))
            val filename: String = URLEncoder.encode("Report-ข้อมูลพื้นฐาน.xlsx", "UTF-8");
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(Util.createExcelFile(fileImportMaster, data))
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

    private fun getSurveySport(): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/sportTournaments", Any::class.java).body as Map<*, *>
        return response["data"]
    }
}