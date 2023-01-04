package ru.tsn.payment.parser

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.payment.model.Payment
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal

class PaymentParser {

    private val SKIP_ROW = 11
    private val ID = 14
    private val DATE = 1
    private val PAYER = 4
    private val SUM = 13
    private val PURPOSE = 20

    fun parse(i: Int, fileName: String, sheetName: String): MutableMap<String, Payment> {
        println("$i. Parse [$fileName]")
        val payments = mutableMapOf<String, Payment>()

        val myFile = File(fileName)
        val fis = FileInputStream(myFile)
        val workbook = XSSFWorkbook(fis)
        val sheet = workbook.getSheet(sheetName)

        var skipCounter = 0
        for (row in sheet) {
            if (skipCounter < SKIP_ROW) {
                skipCounter++
                continue
            }

//            val v = row.getCell(SUM).toString()
//            println(v)
            val payer = row.getCell(PAYER).stringCellValue.trim()
            if (payer.contains("ГЦЖС")) {
                continue
            }

            if (payer.isBlank()) continue
            val docNumber = row.getCell(ID).stringCellValue.trim()
            val date = row.getCell(DATE).localDateTimeCellValue
            val sum = BigDecimal.valueOf(row.getCell(SUM)?.numericCellValue ?: Double.NaN)
            val purpose = row.getCell(PURPOSE).stringCellValue.trim()
            if (purpose.contains("ПО ПРИНЯТЫМ ПЛАТЕЖАМ")
                || purpose.contains("ПО ПЛАТЕЖАМ С")
                || purpose.contains("Возврат депозита по договору")
                || purpose.contains("Выплата %% по договору")) {
                continue
            }

            val uuid = "$date $docNumber $sum";

            payments[uuid] = Payment(uuid, date, payer, sum, docNumber, purpose)
        }
        return payments
    }
}