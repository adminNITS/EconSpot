package com.kkt.worthcalculation.db

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "LogLogin")
data class LogLoginEntity(
    @Id
    @Column(name = "logLoginId", nullable = false)
    var id: String? = null,

    @Column(name = "employeeId", length = 100)
    var employeeId: String? = null,

    @Column(name = "logInAt")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm:ss", locale = "th")
    var logInAt: Date? = null,

    @Column(name = "logoutAt")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm:ss", locale = "th")
    var logoutAt: Date? = null,

    @Column(name = "ipAddress", length = 100)
    var ipAddress: String? = null,

    @Column(name = "browserInfo")
    var browserInfo: String? = null,

    @Column(name = "status")
    var status: String? = null,

    @Column(name = "createDate", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm:ss", locale = "th")
    var createDate: Date? = null,

    @Transient
    var user: Map<*, *>?
)

@Repository
interface LogLoginRepository : JpaRepository<LogLoginEntity, String> {
    fun findAllByIdAndEmployeeId(id: String, employeeId: String): List<LogLoginEntity>
}