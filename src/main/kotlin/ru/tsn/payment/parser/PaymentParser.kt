package ru.tsn.payment.parser

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.payment.enums.RegistryVersionEnum
import ru.tsn.payment.enums.RegistryVersionEnum.*
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
        version: RegistryVersionEnum
    ): MutableMap<String, Payment> {


        val SKIP_ROW = when (version) {
            V1, V3, V4, V5  -> 11
            V2 -> 16
        }
        val ID = 14
        val DATE = when (version) {
            V1, V3, V4, V5 -> 1
            V2 -> 2
        }
        val PAYER = 4
        val SUM = 13
        val BIK_AND_NAME_NUM = 17
        val PURPOSE =
            when (version) {
                V1, V3, V4, V5 -> 20
                V2 -> 19
            }
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
                V1, V4, V5 -> row.getCell(DATE).localDateTimeCellValue
                V2, V3 -> LocalDate.parse(
                    row.getCell(DATE).stringCellValue.toString().trim(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy")
                ).atStartOfDay()
            }
            val sum = BigDecimal.valueOf(row.getCell(SUM)?.numericCellValue ?: Double.NaN)
            val (bik, bankName) = when (version) {
                V1 -> bikAndNameParser(row.getCell(BIK_AND_NAME_NUM).stringCellValue.trim())
                V2, V3, V4, V5 -> bikAndNameParserV2orV3(row.getCell(BIK_AND_NAME_NUM).stringCellValue.trim())
            }
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

    //БИК 042202603, ВОЛГО-ВЯТСКИЙ БАНК ПАО СБЕРБАНК Г. Нижний Новгород
    private fun bikAndNameParser(bikAndName: String): Pair<String, String> {
        val split = bikAndName.split(",")
        val bik = split[0].substring(4)
        val name = split[1].substring(1)
        return Pair(bik, name)
    }

    //БИК 044525232 ПАО "МТС-Банк", г.Москва
    fun bikAndNameParserV2orV3(bikAndName: String): Pair<String, String?> {
        val split = bikAndName.split(" ", limit = 3)
        val bik = split[1]
        val name = if (split.size > 2) split[2] else null
        return Pair(bik, name)
    }
}