package com.kkt.worthcalculation.util

import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ExcelRowData
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.FileInputStream


class ReadImportFileUtil {
    companion object {
        private val log = getLogger(ReadImportFileUtil::class.java)
        fun readFromExcelFile(file: MultipartFile): ExcelRowData {

            try {
                val excelFile = file.inputStream as FileInputStream
                val xlWb = WorkbookFactory.create(excelFile)
                val xlWs = xlWb.getSheetAt(0)
                val evaluator: FormulaEvaluator = xlWb.creationHelper.createFormulaEvaluator()

                val exTournamentName = xlWs.getRow(4).getCell(1)
                val exTournamentLocation = xlWs.getRow(4).getCell(2)
                val exTournamentPeriodDate = xlWs.getRow(4).getCell(3)
                val exTournamentTotalSpendSum = evaluator.evaluateInCell(xlWs.getRow(4).getCell(9))
                val exTournamentBudgetValue = evaluator.evaluateInCell(xlWs.getRow(4).getCell(17))
                val exTournamentNetWorthValue = evaluator.evaluateInCell(xlWs.getRow(4).getCell(42))
                val exTournamentEconomicValue = evaluator.evaluateInCell(xlWs.getRow(4).getCell(43))
                val exTournamentTotalSpend = evaluator.evaluateInCell(exTournamentTotalSpendSum)

                if (exTournamentName.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentName is Invalid")
                if (exTournamentLocation.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentLocation is Invalid")
                if (exTournamentPeriodDate.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentPeriodDate is Invalid")
                if (exTournamentTotalSpend.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentTotalSpend is Invalid")
                if (exTournamentBudgetValue.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentBudgetValue is Invalid")
                if (exTournamentNetWorthValue.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentNetWorthValue is Invalid")
                if (exTournamentEconomicValue.stringCellValue.isEmpty())
                    throw ImportExcelException("exTournamentEconomicValue is Invalid")

                return ExcelRowData(
                    exTournamentName = exTournamentName.stringCellValue,
                    exTournamentLocation = exTournamentLocation.stringCellValue,
                    exTournamentPeriodDate = exTournamentPeriodDate.stringCellValue.replace(" ", ""),
                    exTournamentTotalSpend = exTournamentTotalSpend.toString(),
                    exTournamentBudgetValue = exTournamentBudgetValue.toString(),
                    exTournamentNetWorthValue = exTournamentNetWorthValue.toString(),
                    exTournamentEconomicValue = exTournamentEconomicValue.toString(),
                )
            }catch (e: ImportExcelException) {
                log.error(e.message)
                throw ImportExcelException(e.message.toString())
            } catch (e: Exception) {
                log.error(e.message)
                throw ImportExcelException("Invalid format or field in excel")
            }
        }

        private fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)
    }


}

