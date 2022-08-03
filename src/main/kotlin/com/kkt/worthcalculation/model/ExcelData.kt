package com.kkt.worthcalculation.model

data class ExcelData(
    val tournamentName: String,
    val excelBudgetValue: String? = "0",
    val excelNetWorthValue: String? = "0",
    val excelEconomicValue: String? = "0",
    val excelTotalSpend: String? = "0"
)
