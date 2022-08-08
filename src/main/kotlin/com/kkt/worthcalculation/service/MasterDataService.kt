package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.constant.TextConstant
import com.kkt.worthcalculation.db.IrrMasterEntity
import com.kkt.worthcalculation.db.IrrMasterRepository
import com.kkt.worthcalculation.model.client.Pagination
import com.kkt.worthcalculation.model.client.RequestMasterIRRModel
import com.kkt.worthcalculation.model.client.ResponseModel
import com.kkt.worthcalculation.util.Util
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*


@Service
class MasterDataService(val irrMasterRepo: IrrMasterRepository) {

    private val logger = LoggerFactory.getLogger(javaClass.name)

    fun getListMasterIrr(pagination: Pagination): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>

        try {
            val pageable: Pageable = PageRequest.of(pagination.page - 1, pagination.pageSize, Sort.by(Sort.Direction.DESC, "effectiveDate"))
            val result = irrMasterRepo.findAll(pageable)
            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = result.get(),
                    pagination = Pagination(
                        totalPage = result.totalPages,
                        totalRows = result.totalElements.toInt(),
                        page = result.number + 1,
                        pageSize = result.size
                    )
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

    fun createMasterIrr(req: RequestMasterIRRModel): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>

        try {
            val result = irrMasterRepo.save(
                IrrMasterEntity(
                    id = UUID.randomUUID().toString(),
                    effectiveDate = Util.convertStringToDate(req.effectiveDate),
                    terminateDate = Util.convertStringToDate(req.terminateDate),
                    interest = req.interestRate,
                    createBy = req.actionUserId,
                    createDate = Date(),
                    updateBy = null,
                    updateDate = null,

                    )
            )
            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = result,
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

    fun updateMasterIrr(req: RequestMasterIRRModel, id: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>
        try {
            val data = irrMasterRepo.findById(id)
            if (data.isPresent) {
                val result = irrMasterRepo.save(
                    IrrMasterEntity(
                        id = id,
                        effectiveDate = Util.convertStringToDate(req.effectiveDate),
                        terminateDate = Util.convertStringToDate(req.terminateDate),
                        interest = req.interestRate,
                        createBy = data.get().createBy,
                        createDate = data.get().createDate,
                        updateBy = req.actionUserId,
                        updateDate = Date(),

                        )
                )
                response = ResponseEntity.ok(
                    ResponseModel(
                        message = TextConstant.RESP_SUCCESS_DESC,
                        status = TextConstant.RESP_SUCCESS_STATUS,
                        timestamp = LocalDateTime.now(),
                        data = result,
                        pagination = null
                    )
                )
            } else {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(
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

    fun deleteMasterIrr(id: String): ResponseEntity<ResponseModel> {
        var response: ResponseEntity<ResponseModel>

        try {
            irrMasterRepo.deleteById(id)
            response = ResponseEntity.ok(
                ResponseModel(
                    message = TextConstant.RESP_SUCCESS_DESC,
                    status = TextConstant.RESP_SUCCESS_STATUS,
                    timestamp = LocalDateTime.now(),
                    data = null,
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
}