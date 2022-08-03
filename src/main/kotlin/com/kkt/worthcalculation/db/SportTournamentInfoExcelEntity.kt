package com.kkt.worthcalculation.db

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SportTournamentInfoExcel")
data class SportTournamentInfoExcelEntity(
    @Id
    val id: String,
    val surveySportId: String,
    val excelLocation: String,
    val excelPeriodDate: String,
    val provinceCode: String,
    val excelSportProject: String,
    val excelBudgetValue: String? = "0",
    val excelNetWorthValue: String? = "0",
    val excelEconomicValue: String? = "0",
    val excelTotalSpend: String? = "0",
    val excelFileName: String?,
    @Lob
    var excelData: ByteArray?,
    val excelContentType: String?,
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm", locale = "th")
    val createDate: Date?,
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMMMM-yyyy HH:mm", locale = "th")
    val updateDate: Date?,
    val createBy: String? = "000001",
    val updateBy: String? = "000001",
    @Transient
    var sportTournament: Any?,
    @Transient
    var user: Any?

)

@Repository
interface SportTournamentInfoExcelRepository : JpaRepository<SportTournamentInfoExcelEntity, String>, JpaSpecificationExecutor<SportTournamentInfoExcelEntity> {
    //    fun findBySportTournamentIdAndExcelLocationAndExcelPeriodDate(sportTournamentId: String, excelLocation: String, excelPeriodDate: String): List<SportTournamentInfoExcelEntity>
    fun findAllBySurveySportIdOrderByCreateDateDesc(surveySportId: String): List<SportTournamentInfoExcelEntity>

}
