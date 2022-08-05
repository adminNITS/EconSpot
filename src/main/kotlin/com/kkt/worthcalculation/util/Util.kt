package com.kkt.worthcalculation.util

import com.kkt.worthcalculation.handle.ImportExcelException
import com.kkt.worthcalculation.model.ExcelRowData
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

        fun createExcelFile(file: InputStream, data: Any): ByteArray {

            val info = data as ArrayList<Map<*, *>>
            val xlWb = WorkbookFactory.create(file)
            val sheet = xlWb.getSheetAt(0)

            val exportAtRow: Row = sheet.getRow(0)
            exportAtRow.getCell(1).setCellValue(convertDateTimeFormat())
            val loginAtRow: Row = sheet.getRow(3)
            loginAtRow.getCell(1).setCellValue("${convertDateFormat()} - ${convertDateFormat()} ")

            // Body
            var rowIdx = 6
            var rowNumber = 1

            val dd = sheet.getRow(6).getCell(0).cellStyle
            for (s: Map<*, *>? in info) {
                val sportAct = s?.get("sportAct") as Map<*, *>
                val sportType = s?.get("sportType") as Map<*, *>
                val createUser = s?.get("createUser") as Map<*, *>
                val updateUser = s?.get("updateUser") as Map<*, *>?
                val bodyRow: Row = sheet.createRow(rowIdx)
                bodyRow.rowStyle = dd
                bodyRow.height = 350

                sheet.autoSizeColumn(1)
                sheet.autoSizeColumn(2)
                sheet.autoSizeColumn(3)
                sheet.autoSizeColumn(4)
                sheet.autoSizeColumn(5)
                sheet.autoSizeColumn(6)
                sheet.autoSizeColumn(7)
                bodyRow.createCell(0).setCellValue(rowNumber.toString())
                bodyRow.createCell(1).setCellValue(sportAct["sportActName"].toString())
                bodyRow.createCell(2).setCellValue(sportType["sportTypeName"].toString())
                bodyRow.createCell(3).setCellValue(s["sportTourName"].toString())
                bodyRow.createCell(4).setCellValue(convertDateTimeFormatTH(s["createDate"].toString()))
                bodyRow.createCell(5).setCellValue("${createUser["fname"]} ${createUser["lname"]}")
                bodyRow.createCell(6).setCellValue(convertDateTimeFormatTH(s["updateDate"].toString()))
                bodyRow.createCell(7).setCellValue("${updateUser?.get("fname") ?: ""} ${updateUser?.get("lname") ?: ""}")
                for (i in 0..7) {
                    bodyRow.getCell(i).cellStyle = dd
                }
                rowNumber++
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

        private fun convertDateTimeFormatTH(dateStr: String?): String {
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

