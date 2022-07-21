package com.kkt.worthcalculation.model

data class ExcelRowData(
    val exTournamentName: String,
    val exTournamentLocation: String,
    val exTournamentPeriodDate: String,
    val exTournamentBudgetValue: String? = "0",
    val exTournamentNetWorthValue: String? = "0",
    val exTournamentEconomicValue: String? = "0",
    val exTournamentTotalSpend: String? = "0"
)
