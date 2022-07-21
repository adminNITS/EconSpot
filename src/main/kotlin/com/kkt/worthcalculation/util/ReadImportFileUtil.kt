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

                if (exTournamentName.stringCellValue.isEmpty() || exTournamentLocation.stringCellValue.isEmpty() || exTournamentPeriodDate.stringCellValue.isEmpty())
                    throw Exception("exTournamentName or exTournamentLocation or exTournamentPeriodDate is Invalid")

                val exTournamentTotalSpendSum = evaluator.evaluateInCell(xlWs.getRow(4).getCell(9))
                val exTournamentBudgetValue = evaluator.evaluateInCell(xlWs.getRow(4).getCell(17))
                val exTournamentNetWorthValue = evaluator.evaluateInCell(xlWs.getRow(4).getCell(42))
                val exTournamentEconomicValue = evaluator.evaluateInCell(xlWs.getRow(4).getCell(43))
                val exTournamentTotalSpend = evaluator.evaluateInCell(exTournamentTotalSpendSum)
                return ExcelRowData(
                    exTournamentName = exTournamentName.stringCellValue,
                    exTournamentLocation = exTournamentLocation.stringCellValue,
                    exTournamentPeriodDate = exTournamentPeriodDate.stringCellValue.replace(" ", ""),
                    exTournamentTotalSpend = exTournamentTotalSpend.toString(),
                    exTournamentBudgetValue = exTournamentBudgetValue.toString(),
                    exTournamentNetWorthValue = exTournamentNetWorthValue.toString(),
                    exTournamentEconomicValue = exTournamentEconomicValue.toString(),
                )
            } catch (e: Exception) {
                log.error(e.message)
                throw ImportExcelException("Invalid format or field in excel")
            }
        }

        private fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)
    }


}

