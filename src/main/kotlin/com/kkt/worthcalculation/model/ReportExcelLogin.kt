package com.kkt.worthcalculation.model

data class ReportExcelLogin(
    val employeeCode: String,
    val employeeName: String,
    val groupType: String,
    val username: String,
    val status: String,
    val loginAt: String,
    val logoutAt: String,
    val ip: String,
    val browser: String
)
