package ru.tsn.payment.parser

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.payment.model.Payment
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PaymentParser {

    fun parse(
        i: Int, fileName: String,
        sheetName: String,
    ): MutableMap<String, Payment> {

        println("$i. Parse [$fileName]")
        val payments = mutableMapOf<String, Payment>()

        val myFile = File(fileName)
        val fis = FileInputStream(myFile)
        val workbook = XSSFWorkbook(fis)
        val sheet = workbook.getSheet(sheetName)

        val ID = 14
        val PAYER = 4
        val SUM = 13
        val BIK_AND_NAME_NUM = 17
        val findDate = findMarker("Дата проводки", sheet)
        val findPurpose = findMarker("Назначение платежа", sheet)

        var skipCounter = 0
        for (row in sheet) {
            if (skipCounter < findDate.first) {
                skipCounter++
                continue
            }

            val payer = row.getCell(PAYER).stringCellValue.trim()
            if (payer.contains("ГЦЖС")) {
                continue
            }

            if (payer.isBlank()) continue
            val docNumber = row.getCell(ID).stringCellValue.trim()
            val cellType = row.getCell(findDate.second).cellType

            val date = when (cellType) {
                CellType.NUMERIC -> row.getCell(findDate.second).localDateTimeCellValue
                CellType.STRING -> LocalDate.parse(
                    row.getCell(findDate.second).stringCellValue.toString().trim(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                ).atStartOfDay()

                else -> throw IllegalArgumentException("Unknown cell type")
            }
            val sum = BigDecimal.valueOf(row.getCell(SUM)?.numericCellValue ?: Double.NaN)
            val (bik, bankName) = bikAndNameParser(row.getCell(BIK_AND_NAME_NUM).stringCellValue.trim())
//            println("bik: $bik, bankName: $bankName")
            val purpose = row.getCell(findPurpose.second).stringCellValue.trim()
            if (exclude(purpose)) continue

            val uuid = "$date $docNumber $sum";

            payments[uuid] = Payment(uuid, date, payer, sum, docNumber, purpose)
        }
        return payments
    }

    private fun exclude(purpose: String): Boolean = purpose.contains("ПО ПРИНЯТЫМ ПЛАТЕЖАМ")
            || purpose.contains("ПО ПЛАТЕЖАМ С")
            || purpose.contains("Возврат депозита по договору")
            || purpose.contains("Выплата %% по договору")
            || purpose.contains("ПЕРЕВОД СРЕДСТВ ПО ПОРУЧЕНИЮ ФИЗ.ЛИЦ ЗА")
            || purpose.contains("Оплата по договору D210200334-21")
            || purpose.contains("Возврат денежных средств за заказ")

    //1) БИК 042202603, ВОЛГО-ВЯТСКИЙ БАНК ПАО СБЕРБАНК Г. Нижний Новгород
    //2) БИК 044525232 ПАО "МТС-Банк", г.Москва
    //1) bik = 042202603, bankName = ВОЛГО-ВЯТСКИЙ БАНК ПАО СБЕРБАНК Г. Нижний Новгород
    //2) bik = 044525232, bankName = ПАО "МТС-Банк"
    private fun bikAndNameParser(bikAndName: String): Pair<String, String> {
        val split = bikAndName.split(" ")
        val bik = split[1].replace(",", "").trim()
        val name = split.subList(2, split.size).joinToString(" ")
        return Pair(bik, name)
    }

    private fun findMarker(marker: String, sheet: XSSFSheet): Pair<Int, Int> {
        var i = 0;
        var j = 0;
        for (row in sheet) {
            i++
            for (cell in row) {
                j++
                if (cell.cellType != CellType.STRING) continue
                if (cell.stringCellValue.isBlank()) continue
                if (cell.stringCellValue.contains(marker)) {
                    return Pair(i + 1, j - 1)
                }
            }
            j = 0
        }
        throw IllegalArgumentException("Marker not found: $marker")
    }
}