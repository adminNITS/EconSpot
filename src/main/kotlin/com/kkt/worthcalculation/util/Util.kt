package com.kkt.worthcalculation.util

import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.*
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

        fun writeExcelFile(file: InputStream, data: GenerateExcelData): ByteArray {
            try {
                val xlWb = WorkbookFactory.create(file)
                val xlWs = xlWb.getSheetAt(0)
                xlWs.getRow(4).getCell(1).setCellValue(data.b5)
                xlWs.getRow(4).getCell(2).setCellValue(data.c5)
                xlWs.getRow(4).getCell(3).setCellValue(data.d5)
                xlWs.getRow(4).getCell(5).setCellValue(data.f5.toDouble())
                xlWs.getRow(4).getCell(6).setCellValue(data.g5.toDouble())
                xlWs.getRow(4).getCell(10).setCellValue(data.k5.toDouble())
                xlWs.getRow(4).getCell(11).setCellValue(data.l5.toDouble())
                xlWs.getRow(4).getCell(12).setCellValue(data.m5.toDouble())
                xlWs.getRow(4).getCell(13).setCellValue(data.n5.toDouble())
                xlWs.getRow(4).getCell(14).setCellValue(data.o5.toDouble())
                xlWs.getRow(4).getCell(15).setCellValue(data.p5.toDouble())
                xlWs.getRow(4).getCell(18).setCellValue(data.s5.toDouble())
                xlWs.getRow(4).getCell(19).setCellValue(data.t5.toDouble())
                xlWs.getRow(4).getCell(20).setCellValue(data.u5.toDouble())
                xlWs.getRow(4).getCell(21).setCellValue(data.v5.toDouble())
                xlWs.getRow(4).getCell(22).setCellValue(data.w5.toDouble())
                xlWs.getRow(4).getCell(23).setCellValue(data.x5.toDouble())
                xlWs.getRow(4).getCell(24).setCellValue(data.y5.toDouble())
                xlWs.getRow(4).getCell(25).setCellValue(data.z5.toDouble())
                xlWs.getRow(4).getCell(28).setCellValue(data.ac5.toDouble())
                xlWs.getRow(4).getCell(29).setCellValue(data.ad5.toDouble())
                xlWs.getRow(4).getCell(30).setCellValue(data.ae5.toDouble())
                xlWs.getRow(4).getCell(32).setCellValue(data.ag5.toDouble())
                xlWs.getRow(4).getCell(40).setCellValue(data.ao5.toDouble())
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
            exportAtRow.getCell(1).setCellValue(convertDateTimeFormat(Date()))
            val loginAtRow: Row = sheet.getRow(3)
            loginAtRow.getCell(1).setCellValue("${convertDateFormat(Date())} - ${convertDateFormat(Date())} ")

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
            exportAtRow.getCell(0).setCellValue("Export At : ${convertDateTimeFormat(Date())}")
            val loginAtRow: Row = sheet.getRow(3)
            loginAtRow.createCell(0).setCellValue("Login At : ")
            loginAtRow.createCell(1).setCellValue("${convertDateFormat(Date())} - ${convertDateFormat(Date())} ")
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

        fun createExcelFileC(file: InputStream, data: MutableList<ReportExcelLogin>): ByteArray {

            val xlWb = WorkbookFactory.create(file)
            val sheet = xlWb.getSheetAt(0)

            val exportAtRow: Row = sheet.getRow(0)
            exportAtRow.getCell(0).setCellValue("Export At : ${convertDateTimeFormat(Date())}")
            val loginAtRow: Row = sheet.getRow(3)
            loginAtRow.createCell(0).setCellValue("Login At : ")
            loginAtRow.createCell(1).setCellValue("${convertDateFormat(Date())} - ${convertDateFormat(Date())} ")
            // Body
            var rowIdx = 5
            var cloneStyle = xlWb.createCellStyle()
            for (s: ReportExcelLogin in data) {
                val bodyRow: Row = sheet.createRow(rowIdx)
                bodyRow.height = 350
                for (i in 0..8) {
                    if (rowIdx == 5)
                        cloneStyle = sheet.getRow(6).getCell(i).cellStyle
                    if (rowIdx >= 6)
                        cloneStyle = sheet.getRow(5).getCell(i).cellStyle
                    sheet.autoSizeColumn(i)
                    bodyRow.createCell(i).cellStyle = cloneStyle
                    bodyRow.getCell(i).row.height = -1
                }

                bodyRow.getCell(0).setCellValue(s.status)
                bodyRow.getCell(1).setCellValue(s.loginAt)
                bodyRow.getCell(2).setCellValue(s.logoutAt)
                bodyRow.getCell(3).setCellValue(s.username)
                bodyRow.getCell(4).setCellValue(s.employeeCode)
                bodyRow.getCell(5).setCellValue(s.employeeName)
                bodyRow.getCell(6).setCellValue(s.groupType)
                bodyRow.getCell(7).setCellValue(s.ip)
                bodyRow.getCell(8).setCellValue(s.browser)
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

        fun convertDateTimeFormat(dateStr: String?): Date? {
            if (dateStr.equals("null")) return null
            val sdf1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            return sdf1.parse(dateStr)
        }


        fun convertDateTimeFormat(date: Date): String {
            val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm")
            return sdf2.format(date)
        }

        fun convertDateTimeSecondFormat(date: Date?): String {
            if (date == null) return ""
            val sdf2 = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            return sdf2.format(date)
        }

        private fun convertDateFormat(date: Date): String {
            val sdf2 = SimpleDateFormat("dd/MM/yyyy")
            return sdf2.format(date)
        }

        fun convertStringToDate(strDate: String?): Date? {
            if (strDate.isNullOrEmpty()) return null
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            return sdf.parse(strDate)
        }

    }


}

