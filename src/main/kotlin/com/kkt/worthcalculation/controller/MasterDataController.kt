package com.kkt.worthcalculation.controller

import com.kkt.worthcalculation.model.client.Pagination
import com.kkt.worthcalculation.model.client.RequestMasterIRRModel
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.service.MasterDataService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RequestMapping("api/v2/master")
@RestController
@CrossOrigin
class MasterDataController(val masterDataService: MasterDataService) {

    private val logger = LoggerFactory.getLogger(javaClass.name)


    @GetMapping("/irr/{page}/{pageSize}")
    fun getMasterIrr(@PathVariable page: Int, @PathVariable pageSize: Int): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        return masterDataService.getListMasterIrr(
            Pagination(
                pageSize = pageSize,
                page = page
            )
        )
    }

    @PostMapping("/irr")
    fun addMasterIrr(@Valid @RequestBody req: RequestMasterIRRModel): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("Request: InterestRate: ${req.interestRate}, EffectiveDate: ${req.effectiveDate}, TerminateDate: ${req.terminateDate}")
        return masterDataService.createMasterIrr(req)
    }

    @DeleteMapping("/irr/{id}")
    fun deleteMasterIrr(@PathVariable id: String): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("Request: Id: $id")
        return masterDataService.deleteMasterIrr(id)
    }

    @PostMapping("/irr/{id}")
    fun updateMasterIrr(@Valid @RequestBody req: RequestMasterIRRModel, @PathVariable id: String): ResponseEntity<ResponseModel> {
        MDC.put("trackId", UUID.randomUUID().toString())
        logger.info("Request: Id: $id, InterestRate: ${req.interestRate}, EffectiveDate: ${req.effectiveDate}, TerminateDate: ${req.terminateDate}")
        return masterDataService.updateMasterIrr(req, id)
    }
}