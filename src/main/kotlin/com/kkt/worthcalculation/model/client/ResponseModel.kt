package com.kkt.worthcalculation.model.client

import java.time.LocalDateTime

data class ResponseModel(
    val message: String,
    val status: String,
    val timestamp: LocalDateTime,
    val pagination: Pagination?,
    val data: Any?
) {
}