package com.kkt.worthcalculation.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "SportTournamentInfoExcel")
data class SportTournamentInfoExcelEntity(
    @Id
    val id: String,
    val sportTournamentId: String,
    val excelLocation: String,
    val excelPeriodDate: String,
    val excelBudgetValue: String? = "0",
    val excelNetWorthValue: String? = "0",
    val excelEconomicValue: String? = "0",
    val excelTotalSpend: String? = "0",
    val excelFileName: String?,
    @Lob
    val excelData: ByteArray?,
    val excelContentType: String?,
    @Temporal(TemporalType.TIMESTAMP)
    val createDate: Date?,
    @Temporal(TemporalType.TIMESTAMP)
    val updateDate: Date?,
    val createBy: String? = "000001",
    val updateBy: String? = "000001",
    @Transient
    var sportTournament: Any?

) {

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + sportTournamentId.hashCode()
        result = 31 * result + (excelFileName?.hashCode() ?: 0)
        result = 31 * result + (excelData?.contentHashCode() ?: 0)
        result = 31 * result + (excelContentType?.hashCode() ?: 0)
        result = 31 * result + createDate.hashCode()
        result = 31 * result + updateDate.hashCode()
        result = 31 * result + (createBy?.hashCode() ?: 0)
        result = 31 * result + (updateBy?.hashCode() ?: 0)
        return result
    }
}

@Repository
interface SportTournamentInfoExcelRepository : JpaRepository<SportTournamentInfoExcelEntity, String> {
    fun findBySportTournamentIdAndExcelLocationAndExcelPeriodDate(sportTournamentId: String, excelLocation: String, excelPeriodDate: String): List<SportTournamentInfoExcelEntity>
    fun findAllBySportTournamentIdOrderByCreateDateDesc(sportTournamentId: String): List<SportTournamentInfoExcelEntity>
}
