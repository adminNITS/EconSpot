package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.config.ConfigProperties
import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.model.criteria.SurveySportActCriteria
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime


@Service
class SurveyService(
    private val properties: ConfigProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun getListSportAct(request: SurveySportActCriteria): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            var data = getSurveySportAct()
            if (!request.sportActId.isNullOrEmpty()) {
                data = data.filter { s -> s["sportActId"].toString() == request.sportActId } as ArrayList<Map<*, *>>
            }

            if (!request.sportTypeId.isNullOrEmpty()) {
                data = data.filter { s -> s["sportTypeId"].toString() == request.sportTypeId } as ArrayList<Map<*, *>>
            }

            if (!request.sportTourId.isNullOrEmpty()) {
                var aa: ArrayList<Map<*, *>>
                val bb: MutableList<Map<*, *>> = mutableListOf()
                data.forEach { i ->
                    aa = i["surveySport"] as ArrayList<Map<*, *>>
                    aa = aa.filter { b -> b["sportTourId"].toString() == request.sportTourId } as ArrayList<Map<*, *>>
                    if (aa.isNotEmpty())
                        bb.add(i)
                }
                data = bb as ArrayList<Map<*, *>>
            }

            if (!request.location.isNullOrEmpty()) {
                var aa: ArrayList<Map<*, *>>
                val bb: MutableList<Map<*, *>> = mutableListOf()
                data.forEach { i ->
                    aa = i["surveySport"] as ArrayList<Map<*, *>>
                    aa = aa.filter { b -> b["location"].toString() == request.location } as ArrayList<Map<*, *>>
                    if (aa.isNotEmpty())
                        bb.add(i)
                }
                data = bb as ArrayList<Map<*, *>>
            }



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

    private fun getSurveySportAct(): ArrayList<Map<*, *>> {
        val restTemplate = RestTemplate()
        val response = restTemplate.getForEntity("${properties.existingHost}/rest/surveySportsActives", Any::class.java).body as Map<*, *>
        return response["data"] as ArrayList<Map<*, *>>
    }

}

