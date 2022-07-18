package com.kkt.worthcalculation.model.client

import javax.validation.constraints.NotBlank

class RequestModel {

    @get: NotBlank
    val param1: String = ""

    @get: NotBlank
    val param2: String = ""


}