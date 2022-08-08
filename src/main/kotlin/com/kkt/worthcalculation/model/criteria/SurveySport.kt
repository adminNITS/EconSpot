package com.kkt.worthcalculation.model.criteria

import javax.validation.constraints.NotBlank

class SurveySport {
    @get: NotBlank
    val sportTourId: String = ""
    val sportProject: String? = null
    val location: String? = null
    val provinceCode: String? = null
    override fun toString(): String {
        return "surveySport(sportTourId='$sportTourId', sportProject=$sportProject, location=$location, provinceCode=$provinceCode)"
    }

}