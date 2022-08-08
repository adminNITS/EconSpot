package com.kkt.worthcalculation.model.client

import javax.validation.constraints.NotBlank

class RequestMasterIRRModel {

    @get: NotBlank
    val effectiveDate: String = ""

    @get: NotBlank
    val interestRate: String = ""

    @get: NotBlank
    val actionUserId: String = ""

    val terminateDate: String = ""


}