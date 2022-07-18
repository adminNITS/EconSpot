package com.kkt.worthcalculation.model

data class SurveyCompare(
    val svCompareId: String,
    val sportTournamentId: String,
    val sportTournament: Any?,
    val svCompareExcelSourceData: String,
    val svCompareExcelFileName: String,
    val svCompareBudgetValue: String,
    val svCompareNetWorthValue: String,
    val svCompareEconomicValue: String
)
