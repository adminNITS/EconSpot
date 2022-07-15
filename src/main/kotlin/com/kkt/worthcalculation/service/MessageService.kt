package com.kkt.worthcalculation.service

import com.kkt.worthcalculation.db.Test
import com.kkt.worthcalculation.db.TestRepository
import org.springframework.stereotype.Service

@Service
class MessageService(val db: TestRepository) {

    fun findMessages(): List<Test> = db.findAll()

    fun post(test: Test){
        db.save(test)
    }
}