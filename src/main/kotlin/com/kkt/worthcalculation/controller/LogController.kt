package com.kkt.worthcalculation.controller

import com.kkt.worthcalculation.db.LogLoginEntity
import com.kkt.worthcalculation.db.LogLoginRepository
import com.kkt.worthcalculation.model.criteria.LogRequest
import com.kkt.worthcalculation.util.Util
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.InetAddress
import java.util.*
import javax.servlet.http.HttpServletRequest

@RequestMapping("api/v2/log")
@RestController
class LogController(val logLoginRepository: LogLoginRepository) {
    private val logger = LoggerFactory.getLogger(javaClass.name)

    @PostMapping("/login")
    fun login(@RequestBody logRequest: LogRequest, requestModel: HttpServletRequest): ResponseEntity<LogRequest> {
        MDC.put("trackId", UUID.randomUUID().toString())
        val ipAddress: String = InetAddress.getLocalHost().hostAddress
        val userAgent = requestModel.getHeader("User-Agent")
        val datetimeNow = Date()
        val loginAt = Util.convertDateTimeFormat(datetimeNow)
        logger.info("IP: $ipAddress, Browser: $userAgent, actionUserId: ${logRequest.actionUserId}, LoginAt: $loginAt")
        val result = logLoginRepository.save(
            LogLoginEntity(
                id = UUID.randomUUID().toString(),
                employeeId = logRequest.actionUserId,
                logInAt = datetimeNow,
                logoutAt = null,
                ipAddress = ipAddress,
                browserInfo = userAgent,
                createDate = Date(),
                status = logRequest.status,
                user = null
            )
        )
        logRequest.logId = result.id.toString()

        return ResponseEntity.ok(logRequest)
    }

    @PostMapping("/logout")
    fun logout(@RequestBody logRequest: LogRequest, requestModel: HttpServletRequest): ResponseEntity<Any> {
        MDC.put("trackId", UUID.randomUUID().toString())
        val ipAddress: String = InetAddress.getLocalHost().hostAddress
        val userAgent = requestModel.getHeader("User-Agent")
        val datetimeNow = Date()
        val logoutAt = Util.convertDateTimeFormat(datetimeNow)
        logger.info("IP: $ipAddress, Browser: $userAgent, actionUserId: ${logRequest.actionUserId}, LogoutAt: $logoutAt")
        try {
            val data = logLoginRepository.findAllByIdAndEmployeeId(logRequest.logId, logRequest.actionUserId)
            if (data.isEmpty()) throw Exception("logId not found!!")
            logLoginRepository.save(
                LogLoginEntity(
                    id = data[0].id,
                    employeeId = data[0].employeeId,
                    logInAt = data[0].logInAt,
                    logoutAt = datetimeNow,
                    ipAddress = data[0].ipAddress,
                    browserInfo = data[0].browserInfo,
                    createDate = data[0].createDate,
                    status = data[0].status,
                    user = null
                )
            )
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("not found logId")
        }
        return ResponseEntity.ok(logRequest)
    }
}