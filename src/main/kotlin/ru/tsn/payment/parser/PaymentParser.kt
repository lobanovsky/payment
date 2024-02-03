package ru.tsn.payment.parser

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.payment.enums.RegistryVersionEnum
import ru.tsn.payment.model.Payment
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PaymentParser {

    fun parse(
        i: Int, fileName: String,
        sheetName: String,
        version: RegistryVersionEnum
    ): MutableMap<String, Payment> {


        val SKIP_ROW = if (version == RegistryVersionEnum.V1) 11 else 16
        val ID = 14
        val DATE = if (version == RegistryVersionEnum.V1) 1 else 2
        val PAYER = 4
        val SUM = 13
        val PURPOSE = if (version == RegistryVersionEnum.V1) 20 else 19

        println("$i. Parse $version [$fileName]")
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

            val payer = row.getCell(PAYER).stringCellValue.trim()
            if (payer.contains("ГЦЖС")) {
                continue
            }

            if (payer.isBlank()) continue
            val docNumber = row.getCell(ID).stringCellValue.trim()
            val date = when (version) {
                RegistryVersionEnum.V1 -> row.getCell(DATE).localDateTimeCellValue
                RegistryVersionEnum.V2 -> LocalDate.parse(
                    row.getCell(DATE).stringCellValue.toString().trim(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                ).atStartOfDay()
                RegistryVersionEnum.UNKNOWN -> LocalDateTime.now()
            }
            val sum = BigDecimal.valueOf(row.getCell(SUM)?.numericCellValue ?: Double.NaN)
            val purpose = row.getCell(PURPOSE).stringCellValue.trim()
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

}