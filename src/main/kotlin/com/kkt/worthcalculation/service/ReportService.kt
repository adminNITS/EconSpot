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
            val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelReportA.toByteArray()))
            val filename: String = URLEncoder.encode("Report-ข้อมูลพื้นฐาน.xlsx", "UTF-8");
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(Util.createExcelFileA(fileImportMaster, data))
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

    fun downloadReportPermission(): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val data = getListUser() ?: throw Exception("Excel not found!!")
            val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelReportB.toByteArray()))
            val filename: String = URLEncoder.encode("Report-ข้อมูลผู้ดูแลระบบ.xlsx", "UTF-8");
            val listExcelPermission: MutableList<ReportExcelPermission> = mutableListOf()
            for (s: Map<*, *> in data) {
                val role = s["role"] as Map<*, *>
                val createUser = s?.get("createUser") as Map<*, *>
                val updateUser = s?.get("updateUser") as Map<*, *>?
                convertRoleString(role)
                listExcelPermission.add(
                    ReportExcelPermission(
                        employeeCode = s["userId"].toString(),
                        employeeName = s["fname"].toString() + " " + s["lname"].toString(),
                        groupType = role["roleName"].toString(),
                        permission = convertRoleString(role),
                        status = if (s["status"]?.equals("1") == true) "Active" else "Inactive",
                        createDate = Util.convertDateTimeFormatTH(s["createDate"].toString()),
                        createBy = ("${createUser["fname"]} ${createUser["lname"]}"),
                        updateDate = Util.convertDateTimeFormatTH(s["updateDate"].toString()),
                        updateBy = ("${updateUser?.get("fname") ?: ""} ${updateUser?.get("lname") ?: ""}")
                    )
                )
            }


            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(Util.createExcelFileB(fileImportMaster, listExcelPermission))
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

    private fun getListUser(): ArrayList<Map<*, *>> {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/searchUsers/1/10000", Any::class.java).body as Map<*, *>
        return response["data"] as ArrayList<Map<*, *>>
    }

    private fun convertRoleString(role: Map<*, *>): String {
        val res: StringBuilder = StringBuilder()
        val accessMaster = role["accessMaster"].toString()
        val accessResult = role["accessResult"].toString()
        val accessSurvey = role["accessSurvey"].toString()
        val accessUser = role["accessMaster"].toString()
        val accessMain = role["accessMaster"].toString()
        val accessMobile = role["accessMobile"].toString()

        res.append("ตอบแบบสอบถามออนไลน์ ${findAccessString(accessMain[0])}").append("\n")
        res.append("จัดการข้อมูลแบบสอบถาม ${findPermissionString(accessSurvey[0])}\nชุดแบบสอบถามในรายการแข่งขัน${findPermissionString(accessSurvey[1])}").append("\n")
        res.append("จัดการผลสำรวจ ${findAccessString(accessResult[0])} \nเปรียบเทียบผลสำรวจ ${findAccessString(accessResult[1])}").append("\n")
        res.append("จัดการข้อมูลประเภทกิจกรรมกีฬา ${findPermissionString(accessMaster[0])}\nจัดการข้อมูลชนิดกีฬา${findPermissionString(accessMaster[1])}\nจัดการข้อมูลรายการแข่งขันกีฬา${findPermissionString(accessMaster[2])}").append("\n")
        res.append("จัดการข้อมูลผู้ใช้งาน ${findPermissionString(accessUser[0])}\nจัดการสิทธิ์และประเภทผู้ใช้งาน${findPermissionString(accessUser[1])}").append("\n")
        res.append("สิทธิ์การเข้าใช้งาน Mobile Application ${findAccessString(accessMobile[0])}")
        return res.toString()
    }

    private fun findPermissionString(param: Char): String {
        if (param.equals('0')) {
            return "ไม่มีสิทธิ์";
        } else if (param.equals('1')) {
            return "ค้นหาเพิ่ม ลบ และแก้ไข"
        } else if (param.equals('2')) {
            return "ค้นหาและดูได้เท่านั้น"
        } else if (param.equals('3')) {
            return "ค้นหาเพิ่ม และแก้ไข"
        }
        return ""
    }

    private fun findAccessString(param: Char): String {
        if (param.equals('0')) {
            return "ไม่มีสิทธิ์";
        } else if (param.equals('1')) {
            return "มีสิทธิ์"
        }
        return ""
    }
}


data class ReportExcelPermission(
    val employeeCode: String,
    val employeeName: String,
    val groupType: String,
    val permission: String,
    val status: String,
    val createDate: String,
    val createBy: String,
    val updateDate: String,
    val updateBy: String
)
