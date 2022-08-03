package com.kkt.worthcalculation.model.criteria

import javax.validation.Valid

class RequestCompareCriteria {
    @get: Valid
    val surveySport: surveySport = surveySport()
    override fun toString(): String {
        return "RequestCompareCriteria(surveySport=$surveySport)"
    }


}


