package com.kkt.worthcalculation.model.criteria

import javax.validation.constraints.NotBlank

data class LogRequest(
    @get: NotBlank
    val actionUserId: String = "",
    val status: String = "",
    var logId: String = ""
) {
}