package ru.tsn.payment.parser

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.payment.model.Payment
import java.io.File
import java.io.FileInputStream

class CarParser {

    private val SHEET_NAME = "Лист1"
    private val SKIP_ROW = 13
    private val PAYER = 4
    private val SUM = 1
    private val PURPOSE = 20

    fun parse(fileName: String): List<Payment> {
        val payments = mutableListOf<Payment>()

        val myFile = File(fileName)
        val fis = FileInputStream(myFile)
        val workbook = XSSFWorkbook(fis)
        val sheet = workbook.getSheet(SHEET_NAME)

        var skipCounter = 0
        for (row in sheet) {
            if (skipCounter < SKIP_ROW) {
                skipCounter++
                continue
            }

            val number = row.getCell(SUM).toString()

            val num = number.split("п/п")[0].split("/")[0].replace(" ", "").trim()
            val trailerNum = trailerNum(number)

            val spaceNumber = space(num)
            val spaceTrailer = space(trailerNum)

            val delimeter = if (spaceTrailer.isNotBlank()) "/" else ""
            println("$spaceNumber $delimeter $spaceTrailer")
//            println("$spaceNumber $delimeter $spaceTrailer ---> $number")
        }
        return payments
    }

    private fun space(s: String): String {
        if (s.isBlank()) return ""
        var r = ""
        var current: Char?
        var prev: Char = Char.MIN_VALUE
        for (char in s) {
            current = char.uppercaseChar()
            if ((current.isLetter() && prev.isDigit()) || (current.isDigit() && prev.isLetter())) {
                r = "$r $current"
            } else {
                r += current
            }
            prev = current
        }
        return r
    }

    private fun trailerNum(s: String): String {
        val trailerNum1 = s.split("п/п")
        val trailerNum2 = s.split("/")
        if (trailerNum1.size > 1) {
            return trailerNum1[1]
        }
        if (trailerNum2.size > 1) {
            return trailerNum2[1]
        }
        return ""
    }
}