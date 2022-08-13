package com.kkt.worthcalculation.model.criteria

data class SurveySportActCriteria(
    val sportActId: String? = null,
    val sportTypeId: String? = null,
    val sportTourId: String? = null,
    val location: String? = null
) {

    override fun toString(): String {
        return "SurveySportActCriteria(sportActId=$sportActId, sportTypeId=$sportTypeId, sportTourId=$sportTourId, location=$location)"
    }
}