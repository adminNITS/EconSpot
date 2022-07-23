package com.kkt.worthcalculation.db

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.Date
import javax.persistence.*

@Entity
@Table(name = "SportTournamentInfoExcel")
data class SportTournamentInfoExcel(
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
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SportTournamentInfoExcel

        if (id != other.id) return false
        if (sportTournamentId != other.sportTournamentId) return false
        if (excelFileName != other.excelFileName) return false
        if (excelData != null) {
            if (other.excelData == null) return false
            if (!excelData.contentEquals(other.excelData)) return false
        } else if (other.excelData != null) return false
        if (excelContentType != other.excelContentType) return false
        if (createDate != other.createDate) return false
        if (updateDate != other.updateDate) return false
        if (createBy != other.createBy) return false
        if (updateBy != other.updateBy) return false

        return true
    }

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
interface SportTournamentInfoExcelRepository : JpaRepository<SportTournamentInfoExcel, String>{
    fun findBySportTournamentIdAndExcelLocationAndExcelPeriodDate(sportTournamentId: String, excelLocation: String, excelPeriodDate: String) : List<SportTournamentInfoExcel>
    fun findAllBySportTournamentIdOrderByCreateDateDesc(sportTournamentId: String): List<SportTournamentInfoExcel>
}
