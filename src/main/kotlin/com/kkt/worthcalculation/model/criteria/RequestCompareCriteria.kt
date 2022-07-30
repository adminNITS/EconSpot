package com.kkt.worthcalculation.model.criteria

import javax.validation.Valid

class RequestCompareCriteria {
    @get: Valid
    val tournamentA: Tournament = Tournament()

    @get: Valid
    val tournamentB: Tournament = Tournament()
    override fun toString(): String {
        return "RequestCompareCriteria(tournamentA=$tournamentA, tournamentB=$tournamentB)"
    }


}


