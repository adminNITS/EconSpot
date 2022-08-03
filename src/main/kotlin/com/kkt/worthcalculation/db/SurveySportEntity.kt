package com.kkt.worthcalculation.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SurveySport")
data class SurveySportEntity(
    @Id
    val surveySportId: String? = null,
    @Column(name = "sportTourId", nullable = true)
    val sportTourId: String? = null,

    @Basic
    @Column(name = "location", nullable = true)
    val location: String? = null,

    @Basic
    @Column(name = "provinceCode", nullable = true)
    val provinceCode: String? = null,

    @Basic
    @Column(name = "teamName", nullable = true)
    val teamName: String? = null,

    @Basic
    @Column(name = "teamAddress", nullable = true)
    val teamAddress: String? = null,

    @Basic
    @Column(name = "teamTel", nullable = true)
    val teamTel: String? = null,

    @Basic
    @Column(name = "teamFax", nullable = true)
    val teamFax: String? = null,

    @Basic
    @Column(name = "startDate", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    val startDate: Date? = null,

    @Basic
    @Column(name = "endDate", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    val endDate: Date? = null,

    @Basic
    @Column(name = "status", nullable = true)
    val status: String? = null,

    @Basic
    @Column(name = "createDate", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    val createDate: Date? = null,

    @Basic
    @Column(name = "createBy", nullable = true)
    val createBy: String? = null,

    @Basic
    @Column(name = "updateDate", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    val updateDate: Date? = null,

    @Basic
    @Column(name = "updateBy", nullable = true)
    val updateBy: String? = null,

    @Basic
    @Column(name = "sportProject", nullable = true)
    val sportProject: String? = null,

    @Transient
    var sportTournament: Any?,

    @Transient
    var sportTournamentSurveyExcel: Any?
)

@Repository
interface SurveySportRepository : JpaRepository<SurveySportEntity, String>, JpaSpecificationExecutor<SurveySportEntity> {
    fun findAllBySportTourIdAndStartDateBetween(sportTourId: String?, startDate: Date?, endDate: Date?): List<SurveySportEntity>
}


