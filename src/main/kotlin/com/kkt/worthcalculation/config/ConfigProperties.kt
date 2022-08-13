package com.kkt.worthcalculation.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application")
data class ConfigProperties(
    val excelImportMaster: String,
    val excelReportGeneral: String,
    val excelReportPermission: String,
    val excelReportLogin: String,
    val existingHost: String,
    val existingHostMongo: String,
    val irrValue: String
)
