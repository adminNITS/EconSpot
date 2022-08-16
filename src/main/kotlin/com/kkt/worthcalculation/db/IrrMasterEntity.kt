package com.kkt.worthcalculation.db

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "IrrMaster")
data class IrrMasterEntity(
    @Id
    @Column(name = "irrId", nullable = false, length = 100)
    var id: String? = null,

    @Column(name = "interest", nullable = false, length = 10)
    var interest: String? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "effectiveDate")
    var effectiveDate: Date? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "terminateDate")
    var terminateDate: Date? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm:ss", locale = "th")
    @Column(name = "createDate")
    var createDate: Date? = null,

    @Column(name = "createBy", length = 256)
    var createBy: String? = null,

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm:ss", locale = "th")
    @Column(name = "updateDate")
    var updateDate: Date? = null,

    @Column(name = "updateBy", length = 256)
    var updateBy: String? = null,

    @Transient
    var userCreateBy: Any?,

    @Transient
    var userUpdateBy: Any?
) {

}

@Repository
interface IrrMasterRepository : JpaRepository<IrrMasterEntity, String>, JpaSpecificationExecutor<IrrMasterEntity> {
}