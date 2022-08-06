package com.kkt.worthcalculation.util

import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ExcelRowData
import com.kkt.worthcalculation.service.ReportExcelGeneral
import com.kkt.worthcalculation.service.ReportExcelPermission
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.NumberToTextConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
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

        fun createExcelFileA(file: InputStream, data: MutableList<ReportExcelGeneral>): ByteArray {

            val xlWb = WorkbookFactory.create(file)
            val sheet = xlWb.getSheetAt(0)
            val exportAtRow: Row = sheet.getRow(0)
            exportAtRow.getCell(1).setCellValue(convertDateTimeFormat())
            val loginAtRow: Row = sheet.getRow(3)
            loginAtRow.getCell(1).setCellValue("${convertDateFormat()} - ${convertDateFormat()} ")

            // Body
            var rowIdx = 6
            var rowNumber = 1
            var cloneStyle = xlWb.createCellStyle()
            for (s: ReportExcelGeneral in data) {
                val bodyRow: Row = sheet.createRow(rowIdx)
                bodyRow.height = 350
                for (i in 0..7) {
                    if (rowIdx == 6)
                        cloneStyle = sheet.getRow(7).getCell(i).cellStyle
                    if (rowIdx >= 7)
                        cloneStyle = sheet.getRow(6).getCell(i).cellStyle
                    sheet.autoSizeColumn(i)
                    bodyRow.createCell(i).cellStyle = cloneStyle
                    bodyRow.getCell(i).row.height = -1
                }

                bodyRow.getCell(0).setCellValue(rowNumber.toString())
                bodyRow.getCell(1).setCellValue(s.activityType)
                bodyRow.getCell(2).setCellValue(s.sportType)
                bodyRow.getCell(3).setCellValue(s.name)
                bodyRow.getCell(4).setCellValue(s.createDate)
                bodyRow.getCell(5).setCellValue(s.createBy)
                bodyRow.getCell(6).setCellValue(s.updateDate)
                bodyRow.getCell(7).setCellValue(s.updateBy)

                rowNumber++
                rowIdx++
            }
            val out = ByteArrayOutputStream()
            xlWb.write(out)
            return out.toByteArray()

        }

        fun createExcelFileB(file: InputStream, data: MutableList<ReportExcelPermission>): ByteArray {

            val xlWb = WorkbookFactory.create(file)
            val sheet = xlWb.getSheetAt(0)

            val exportAtRow: Row = sheet.getRow(0)
            exportAtRow.getCell(0).setCellValue("Export At : ${convertDateTimeFormat()}")
            val loginAtRow: Row = sheet.getRow(3)
            loginAtRow.createCell(0).setCellValue("Login At : ")
            loginAtRow.createCell(1).setCellValue("${convertDateFormat()} - ${convertDateFormat()} ")
            // Body
            var rowIdx = 6
            var cloneStyle = xlWb.createCellStyle()
            for (s: ReportExcelPermission in data) {
                val bodyRow: Row = sheet.createRow(rowIdx)
                bodyRow.height = 350
                for (i in 0..8) {
                    if (rowIdx == 6)
                        cloneStyle = sheet.getRow(7).getCell(i).cellStyle
                    if (rowIdx >= 7)
                        cloneStyle = sheet.getRow(6).getCell(i).cellStyle
                    sheet.autoSizeColumn(i)
                    bodyRow.createCell(i).cellStyle = cloneStyle
                    bodyRow.getCell(i).row.height = -1
                }

                bodyRow.getCell(0).setCellValue(s.employeeCode)
                bodyRow.getCell(1).setCellValue(s.employeeName)
                bodyRow.getCell(2).setCellValue(s.groupType)
                bodyRow.getCell(3).setCellValue(s.permission)
                bodyRow.getCell(4).setCellValue(s.status)
                bodyRow.getCell(5).setCellValue(s.createDate)
                bodyRow.getCell(6).setCellValue(s.createBy)
                bodyRow.getCell(7).setCellValue(s.updateDate)
                bodyRow.getCell(8).setCellValue(s.updateBy)
                rowIdx++
            }
            val out = ByteArrayOutputStream()
            xlWb.write(out)
            return out.toByteArray()

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

        fun convertDateFormatTH(dateStr: String): String {
            val sdf1 = SimpleDateFormat("yyyy-MM-dd")
            val sdf2 = SimpleDateFormat("dd/MMM/yyyy", Locale("th", "th"))
            val date = sdf1.parse(dateStr)
            return sdf2.format(date)
        }

        fun convertDateTimeFormatTH(dateStr: String?): String {
            if (dateStr.equals("null")) return ""
            val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("th", "th"))
            val date = sdf1.parse(dateStr)
            return sdf2.format(date)
        }

        private fun convertDateTimeFormat(): String {
            val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm")
            return sdf2.format(Date())
        }

        private fun convertDateFormat(): String {
            val sdf2 = SimpleDateFormat("dd/MM/yyyy")
            return sdf2.format(Date())
        }

    }


}

