package com.kkt.worthcalculation.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application")
data class ConfigProperties(
    val excelImportMaster: String,
    val excelReportA: String,
    val excelReportB: String
)
