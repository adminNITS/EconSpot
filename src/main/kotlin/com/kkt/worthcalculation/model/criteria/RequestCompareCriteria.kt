package com.kkt.worthcalculation.model.criteria

import javax.validation.Valid

class RequestCompareCriteria {
    @get: Valid
    val tournamentA: Tournament = Tournament()

    @get: Valid
    val tournamentB: Tournament = Tournament()
}


