package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.config.ConfigProperties
import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.db.LogLoginEntity
import com.kkt.worthcalculation.db.LogLoginRepository
import com.kkt.worthcalculation.model.ReportExcelGeneral
import com.kkt.worthcalculation.model.ReportExcelLogin
import com.kkt.worthcalculation.model.ReportExcelPermission
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
    private val properties: ConfigProperties,
    val logLoginRepository: LogLoginRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun downloadReportGeneral(startDate: String, endDate: String): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val data = getSurveySport() ?: throw Exception("Excel not found!!")

            val dateStart = Util.convertStringToDate(startDate)
            val dateEnd = Util.convertStringToDate(endDate)

            val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelReportGeneral.toByteArray()))
            val filename: String = URLEncoder.encode("Report-ข้อมูลพื้นฐาน.xlsx", "UTF-8");
            val listExcelGeneral: MutableList<ReportExcelGeneral> = mutableListOf()
            for (s: Map<*, *> in data) {
                val dateCreateData = Util.convertDateTimeFormat(s["createDate"].toString())
                if (dateCreateData?.after(dateStart) == true && dateCreateData.before(dateEnd)) {
                    val sportAct = s?.get("sportAct") as Map<*, *>
                    val sportType = s?.get("sportType") as Map<*, *>
                    val createUser = s?.get("createUser") as Map<*, *>
                    val updateUser = s?.get("updateUser") as Map<*, *>?
                    listExcelGeneral.add(
                        ReportExcelGeneral(
                            activityType = sportAct["sportActName"].toString(),
                            sportType = sportType["sportTypeName"].toString(),
                            name = s["sportTourName"].toString(),
                            createDate = Util.convertDateTimeFormatTH(s["createDate"].toString()),
                            createBy = "${createUser["fname"]} ${createUser["lname"]}",
                            updateDate = Util.convertDateTimeFormatTH(s["updateDate"].toString()),
                            updateBy = "${updateUser?.get("fname") ?: ""} ${updateUser?.get("lname") ?: ""}"
                        )
                    )
                }
            }




            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(TextConstant.EXCEL_SHEET_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(Util.createExcelFileA(fileImportMaster, listExcelGeneral))
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun downloadReportPermission(startDate: String, endDate: String): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val data = getListUser() ?: throw Exception("Excel not found!!")
            val dateStart = Util.convertStringToDate(startDate)
            val dateEnd = Util.convertStringToDate(endDate)
            val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelReportPermission.toByteArray()))
            val filename: String = URLEncoder.encode("Report-ข้อมูลผู้ดูแลระบบ.xlsx", "UTF-8");
            val listExcelPermission: MutableList<ReportExcelPermission> = mutableListOf()
            for (s: Map<*, *> in data) {
                val dateCreateData = Util.convertDateTimeFormat(s["createDate"].toString())
                if (dateCreateData?.after(dateStart) == true && dateCreateData.before(dateEnd)) {
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
            }


            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(TextConstant.EXCEL_SHEET_CONTENT_TYPE))
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

    fun downloadReportLogin(startDate: String, endDate: String): ResponseEntity<Any> {
        var response: ResponseEntity<Any>
        try {
            val dateStart = Util.convertStringToDate(startDate)
            val dateEnd = Util.convertStringToDate(endDate)
            val data = logLoginRepository.searchAllByCreateDateBetween(dateStart, dateEnd)
            val fileImportMaster: InputStream = ByteArrayInputStream(Base64.getDecoder().decode(properties.excelReportLogin.toByteArray()))
            val filename: String = URLEncoder.encode("Report-ข้อมูลผู้เข้าใช้งาน.xlsx", "UTF-8");
            val listExcelLogin: MutableList<ReportExcelLogin> = mutableListOf()
            for (s: LogLoginEntity in data) {
                s.user = getUser(s.employeeId.toString()) as Map<*, *>
                val role = s.user?.get("role") as Map<*, *>
                listExcelLogin.add(
                    ReportExcelLogin(
                        status = s.status.toString(),
                        employeeName = s.user!!["fname"].toString() + s.user!!["lname"].toString(),
                        groupType = role["roleName"].toString(),
                        ip = s.ipAddress.toString(),
                        browser = s.browserInfo.toString(),
                        username = s.user!!["userName"].toString(),
                        logoutAt = Util.convertDateTimeSecondFormat(s.logoutAt),
                        loginAt = Util.convertDateTimeSecondFormat(s.logInAt),
                        employeeCode = s.employeeId.toString()
                    )
                )
            }


            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(TextConstant.EXCEL_SHEET_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
                .body(Util.createExcelFileC(fileImportMaster, listExcelLogin))
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

    private fun getSurveySport(): ArrayList<Map<*, *>> {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/sportTournaments", Any::class.java).body as Map<*, *>
        return response["data"] as ArrayList<Map<*, *>>
    }

    private fun getListUser(): ArrayList<Map<*, *>> {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/searchUsers/1/10000", Any::class.java).body as Map<*, *>
        return response["data"] as ArrayList<Map<*, *>>
    }

    private fun getUser(userId: String): Any? {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/user/$userId", Any::class.java).body as Map<*, *>
        return response["data"] as Map<*, *>
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


