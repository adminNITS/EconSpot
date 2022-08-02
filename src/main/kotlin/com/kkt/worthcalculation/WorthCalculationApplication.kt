package com.kkt.worthcalculation

import com.kkt.worthcalculation.config.ConfigProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(ConfigProperties::class)
@SpringBootApplication
class WorthCalculationApplication

fun main(args: Array<String>) {
    runApplication<WorthCalculationApplication>(*args)
}
