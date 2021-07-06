package ru.tsn.payment.parser

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import ru.tsn.payment.model.Account
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal

class AccountParser {

    private val SHEET_NAME = "Основные сведения"
    private val SKIP_ROW = 2
    private val ACCOUNT = 1
    private val LAST_NAME = 6
    private val FIRST_NAME = 7
    private val MIDDLE_NAME = 8
    private val SQUARE = 17

    fun parse(fileName: String): List<Account> {
        val accounts = mutableListOf<Account>()

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
//            println(row.getCell(ACCOUNT))
            val account = row.getCell(ACCOUNT).stringCellValue.trim()
            val lastName = row.getCell(LAST_NAME)?.stringCellValue?.trim()
            val firstName = row.getCell(FIRST_NAME)?.stringCellValue?.trim()
            val middleName = row.getCell(MIDDLE_NAME)?.stringCellValue?.substringBefore("(")?.trim()
            val square = row.getCell(SQUARE)?.stringCellValue?.trim()
            accounts.add(Account(account, lastName, firstName, middleName, BigDecimal(square)))
        }
        return accounts
    }
}