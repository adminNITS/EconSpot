package com.kkt.worthcalculation.util

import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ExcelRowData
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.NumberToTextConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.util.*


class Util {
    companion object {
        private val log = getLogger(Util::class.java)
        fun readFromExcelFile(file: MultipartFile): ExcelRowData {
            val excelFile = file.inputStream as FileInputStream
            try {
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
                if (exTournamentTotalSpend.toString().isEmpty())
                    throw ImportExcelException("exTournamentTotalSpend is Invalid")
                if (exTournamentBudgetValue.toString().isEmpty())
                    throw ImportExcelException("exTournamentBudgetValue is Invalid")
                if (exTournamentNetWorthValue.toString().isEmpty())
                    throw ImportExcelException("exTournamentNetWorthValue is Invalid")
                if (exTournamentEconomicValue.toString().isEmpty())
                    throw ImportExcelException("exTournamentEconomicValue is Invalid")

                return ExcelRowData(
                    exTournamentName = exTournamentName.stringCellValue,
                    exTournamentLocation = exTournamentLocation.stringCellValue,
                    exTournamentPeriodDate = exTournamentPeriodDate.stringCellValue.replace(" ", ""),
                    exTournamentTotalSpend = NumberToTextConverter.toText(exTournamentTotalSpend.numericCellValue),
                    exTournamentBudgetValue = NumberToTextConverter.toText(exTournamentBudgetValue.numericCellValue),
                    exTournamentNetWorthValue = NumberToTextConverter.toText(exTournamentNetWorthValue.numericCellValue),
                    exTournamentEconomicValue = NumberToTextConverter.toText(exTournamentEconomicValue.numericCellValue),
                )
            } catch (e: ImportExcelException) {
                log.error(e.message)
                throw ImportExcelException(e.message.toString())
            } catch (e: Exception) {
                log.error(e.message)
                throw ImportExcelException("Invalid format or field in excel")
            } finally {
                excelFile.close()
            }
        }

        fun writeExcelFile(file: InputStream, sportTournamentName: String, location: String, startDate: String, endDate: String): ByteArray {
            try {
                val xlWb = WorkbookFactory.create(file)
                val xlWs = xlWb.getSheetAt(0)
                xlWs.getRow(4).createCell(1).setCellValue(sportTournamentName)
                xlWs.getRow(4).createCell(2).setCellValue(location)
                xlWs.getRow(4).createCell(3).setCellValue("$startDate - $endDate")
                val out = ByteArrayOutputStream()
                xlWb.write(out)
                return out.toByteArray()
            } catch (e: Exception) {
                throw Exception("Cant create excel file " + e.message)
            }
        }

        private fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)

        fun getExtensionByStringHandling(filename: String?): Optional<String?>? {
            if (filename != null) {
                return Optional.ofNullable(filename)
                    .filter { f -> f.contains(".") }
                    .map { f -> f.substring(filename.lastIndexOf(".") + 1) }
            }
            return null
        }
    }


}

