package com.kkt.worthcalculation.model

data class ReportExcelPermission(
    val employeeCode: String,
    val employeeName: String,
    val groupType: String,
    val permission: String,
    val status: String,
    val createDate: String,
    val createBy: String,
    val updateDate: String,
    val updateBy: String
)
