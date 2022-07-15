package com.kkt.worthcalculation.controller

import com.kkt.worthcalculation.db.Test
import com.kkt.worthcalculation.service.MessageService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class MessageResource(val service: MessageService) {
    @GetMapping("/test")
    fun index(): List<Test> = service.findMessages()
}