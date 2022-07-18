package com.kkt.worthcalculation.model

import java.time.LocalDateTime

data class ErrorDetails(val timestamp: LocalDateTime, val message: String, val status:String, val data: Map<String, String?>)