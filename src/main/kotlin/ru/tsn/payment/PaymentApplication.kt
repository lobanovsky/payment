package ru.tsn.payment

import org.apache.commons.io.FilenameUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.tsn.payment.enums.RegistryVersionEnum
import ru.tsn.payment.enums.SearchTypeEnum
import ru.tsn.payment.model.Account
import ru.tsn.payment.model.Payment
import ru.tsn.payment.parser.AccountParser
import ru.tsn.payment.parser.PaymentParser
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.writeLines

//TODO Попов платит за квартиру, а указывает машиноместо 3115 -> 1031
@SpringBootApplication
class PaymentApplication : CommandLineRunner {

    private val DEFAULT_FOLDER = "etc"
//    private val FILE_NAME = "СберБизнес. Выписка за 2021.06.04-2021.06.17 счёт 40703810838000014811.xlsx"
//    private val FILE_NAME = "СберБизнес. Выписка за 2021.06.18-2021.06.26 счёт 40703810838000014811.xlsx"

    private val PAYMENTS = listOf(
        "СберБизнес. Выписка за 2023.01.04-2023.01.16 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.01.16-2023.01.23 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.01.23-2023.01.31 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.01.31-2023.02.27 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.02.27-2023.03.17 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.03.17-2023.03.27 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.03.27-2023.03.31 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.03.31-2023.04.30 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.04.30-2023.05.11 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.05.11-2023.05.23 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.05.23-2023.07.01 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.07.01-2023.07.22 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.07.22-2023.07.26 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.07.26-2023.08.21 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.08.21-2023.08.28 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.08.28-2023.09.11 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.09.11-2023.09.20 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.09.20-2023.09.25 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.09.25-2023.10.01 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.10.01-2023.10.03 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.10.03-2023.10.07 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.10.07-2023.10.12 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.10.12-2023.10.17 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.10.17-2023.10.30 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.10.30-2023.11.14 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.11.14-2023.11.15 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.11.15-2023.11.21 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.11.21-2023.11.22 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.11.22-2023.11.29 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.11.29-2023.12.13 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.12.13-2023.12.18 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.12.18-2023.12.24 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2023.12.24-2024.01.02 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2024.01.02-2024.01.10 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2024.01.10-2024.01.22 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2024.01.22-2024.02.02 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2024.02.02-2024.02.26 счёт 40703810338000004376.xlsx",
        "СберБизнес. Выписка за 2024.02.26-2024.03.20 счёт 40703810338000004376.xlsx",
    )

    private fun getVersion(fileName: String, sheetName: String): RegistryVersionEnum {
        val myFile = File(fileName)
        val fis = FileInputStream(myFile)
        val workbook = XSSFWorkbook(fis)
        val sheet = workbook.getSheet(sheetName)
        val row = sheet.getRow(1)
        val version = row.getCell(5).stringCellValue

        if (version.startsWith("СберБизнес 41.")) return RegistryVersionEnum.V1
        if (version.startsWith("СберБизнес. 03.001.02-15")) return RegistryVersionEnum.V2
        if (version.startsWith("СберБизнес. 03.001.02-18")) return RegistryVersionEnum.V3

        throw IllegalArgumentException("Unknown version of excel document")
    }

    override fun run(vararg args: String?) {
        val ids = mutableSetOf<String>()
        val accounts = AccountParser().parse("etc/ЛС УО1.xlsx")
        var i = 1
        for (file in PAYMENTS) {
            val sheetName = file.substringAfterLast(" ").split(".")[0]
            val fileName = Paths.get(DEFAULT_FOLDER).resolve(file).toString()
            val version = getVersion(fileName, sheetName)
            val payments = PaymentParser().parse(i, fileName, sheetName, version)

            val duplicates = findDuplicates(payments.keys, ids)
            println("Duplicates [${duplicates.size}] $duplicates")

            print("Modify payments: ${payments.size} -> ")
            for (id in duplicates) {
                payments.remove(id)
            }
            println("${payments.size}")

            ids.clear()
            ids.addAll(payments.keys)

            val lines = determine(payments.values, accounts)

//            println(lines)
            println()
            Path(Paths.get(DEFAULT_FOLDER).resolve(fileName(file)).toString()).writeLines(lines);
            i++
        }
    }

    fun findDuplicates(a: Set<String>, b: Set<String>): Set<String> {
        return a intersect b
    }

    private fun fileName(prefix: String): String {
        val baseName = FilenameUtils.getBaseName(prefix)
        val formatterFileName = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateFormat = LocalDateTime.now().format(formatterFileName)
        return "$baseName [2021-12-05].csv"
    }


    fun determine(payments: Collection<Payment>, accounts: List<Account>): List<String> {
        val lines = mutableListOf<String>()
        for (payment in payments) {
            val (account, searchType) = findAccountOrNull(accounts, payment)
//            println(payment)
//            println(account)
//            println("---")
            lines.add(createLine(payment, account, searchType))
        }
        return lines
    }

    fun createLine(payment: Payment, account: Account?, searchTypeEnum: SearchTypeEnum?): String {
        val items = mutableListOf<String?>()
        items.add(account?.number)
        items.add(payment.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        items.add("")
        items.add("")
        items.add("")
        items.add(searchTypeEnum.toString())
        items.add(payment.sum.toString().replace(".", ","))
        items.add(payment.payer.replace("\n", " ").replace(";", ","))
        items.add(payment.purpose.replace(";", ","))
        return items.joinToString(";")
    }

    //не менять очередность строк в данном методе
    private fun findAccountOrNull(accounts: List<Account>, payment: Payment): Pair<Account?, SearchTypeEnum?> {
        var account = findByFullAccount(accounts, payment.purpose)
        if (account != null) return Pair(account, SearchTypeEnum.FULL_ACCOUNT)

        account = findByShortAccount(accounts, payment.purpose)
        if (account != null) return Pair(account, SearchTypeEnum.SHORT_ACCOUNT)

        account = findByFullNameFlat(accounts, payment)
        if (account != null) return Pair(account, SearchTypeEnum.NAME_FLAT)


        account = findByFullNameParking(accounts, payment)
        if (account != null) return Pair(account, SearchTypeEnum.NAME_PARKING)

        account = findByHardCodeOrNull(accounts, payment)
        if (account != null) return Pair(account, SearchTypeEnum.HARD_CODE)

        account = findByPurposeOrNull(accounts, payment)
        if (account != null) return Pair(account, SearchTypeEnum.HARD_CODE)

        return Pair(null, null)
    }

    fun findByFullAccount(accounts: List<Account>, purpose: String?): Account? {
        return accounts.firstOrNull {
            purpose?.contains(it.number, ignoreCase = true) == true
        }
    }

    fun findByShortAccount(accounts: List<Account>, purpose: String?): Account? {
        return accounts.firstOrNull {
            purpose?.contains(" ${it.number.trimStart('0')}", ignoreCase = true) == true
        }
    }

    fun findByFullNameFlat(accounts: List<Account>, payment: Payment?): Account? {
        return accounts.firstOrNull {
            val payer = payment?.payer?.lowercase()?.replace(" ", "")?.trim()
            val fullName = (it.lastName + it.firstName + it.middleName).lowercase().replace(" ", "").trim()
            payer?.contains(fullName) == true && more3000(payment.sum) && squareMore20(it.square)
        }
    }

    fun findByFullNameParking(accounts: List<Account>, payment: Payment?): Account? {
        return accounts.firstOrNull {
            val payer = payment?.payer?.lowercase()?.replace(" ", "")?.trim()
            val fullName = (it.lastName + it.firstName + it.middleName).lowercase().replace(" ", "").trim()
            payer?.contains(fullName) == true && less3000(payment.sum) && squareLess20(it.square)
        }
    }

    fun more3000(sum: BigDecimal?): Boolean = sum?.compareTo(BigDecimal(3000)) == 1
    fun squareMore20(sum: BigDecimal?): Boolean = sum?.compareTo(BigDecimal(20)) == 1
    fun less3000(sum: BigDecimal?): Boolean = sum?.compareTo(BigDecimal(3000)) == -1
    fun squareLess20(sum: BigDecimal?): Boolean = sum?.compareTo(BigDecimal(20)) == -1

    fun findByPurposeOrNull(accounts: List<Account>, payment: Payment?): Account? {
        if (payment?.purpose?.contains("Машиноместо №34", true) == true) {
            return accounts.find { it.number.contains("3034") }
        }
        if (payment?.purpose?.contains("Машиноместо №35", true) == true) {
            return accounts.find { it.number.contains("3035") }
        }
        if (payment?.purpose?.contains("Машиноместо №67", true) == true) {
            return accounts.find { it.number.contains("3067") }
        }
        if (payment?.purpose?.contains("Машиноместо №68", true) == true) {
            return accounts.find { it.number.contains("3068") }
        }
        return null
    }

    fun findByHardCodeOrNull(accounts: List<Account>, payment: Payment?): Account? {
        if (payment?.payer?.contains("ВИЖИЦКИЙ ВАЛЕРИЙ АЛЕКСЕЕВИЧ", true) == true) {
            return accounts.find { it.number.contains("1007") }
        }
        if (payment?.payer?.contains("ПЕСКОВА КСЕНИЯ СЕРГЕЕВНА", true) == true) {
            return accounts.find { it.number.contains("1014") }
        }
        if (payment?.payer?.contains("ДЫБОВ ДЕНИС АЛЕКСАНДРОВИЧ", true) == true) {
            return accounts.find { it.number.contains("1093") }
        }
        if (payment?.payer?.contains("ПОЛОВОДОВ ВИКТОР ПАВЛОВИЧ", true) == true) {
            return accounts.find { it.number.contains("1041") }
        }
        if (payment?.payer?.contains("МУХТАРОВ АРИФ ТОФИКОВИЧ", true) == true) {
            return accounts.find { it.number.contains("1036") }
        }
        if (payment?.payer?.contains("ЛАБЫШКИНА ЕКАТЕРИНА ВЯЧЕСЛАВОВНА", true) == true) {
            return accounts.find { it.number.contains("1061") }
        }
        if (payment?.payer?.contains("ИСАХАНЯН АРАМ ИГНАТОВИЧ", true) == true) {
            return accounts.find { it.number.contains("3102") }
        }
        if (payment?.payer?.contains("ГОГОХИЯ ДАВИД ГОГИЕВИЧ", true) == true) {
            return accounts.find { it.number.contains("1025") }
        }
        if (payment?.payer?.contains("ВИНОГРАДОВА ОЛЬГА ДМИТРИЕВНА", true) == true) {
            return accounts.find { it.number.contains("1109") }
        }
        if (payment?.payer?.contains("ПОЛОВОДОВ ВИКТОР ПАВЛОВИЧ", true) == true) {
            return accounts.find { it.number.contains("1041") }
        }
        if (payment?.payer?.contains("КИШМИШЯН АРМАН АРСЕНОВИЧ", true) == true) {
            return accounts.find { it.number.contains("2003") }
        }
        if (payment?.payer?.contains("КУДЕРЦЕВ КИРИЛЛ ОЛЕГОВИЧ", true) == true) {
            return accounts.find { it.number.contains("1058") }
        }
//        if (payment?.payer?.contains("БОЛЫШЕВА КСЕНИЯ МАКСИМОВНА", true) == true) {
//            return accounts.find { it.number.contains("1109") }
//        }
        if (payment?.payer?.contains("КАРАХАН АКАЙ", true) == true) {
            return accounts.find { it.number.contains("1088") }
        }
        if (payment?.payer?.contains("РОМАНОВСКИЙ ГЕННАДИЙ ГЕОРГИЕВИЧ", true) == true) {
            return accounts.find { it.number.contains("3098") }
        }
        if (payment?.payer?.contains("СПЕКТОР МАРИНА РОМАНОВНА", true) == true) {
            return accounts.find { it.number.contains("1014") }
        }
        if (payment?.payer?.contains("ДОНСКИХ ДЕНИС ГЕННАДЬЕВИЧ", true) == true) {
            return accounts.find { it.number.contains("3020") }
        }
        if (payment?.payer?.contains("Хайбулаев Заур Магомеддибирович", true) == true) {
            return accounts.find { it.number.contains("3097") }
        }
        if (payment?.payer?.contains("Хлебникова Светлана Александровна", true) == true) {
            return accounts.find { it.number.contains("3123") }
        }
        if (payment?.payer?.contains("Коровина Любовь Николаевна", true) == true) {
            return accounts.find { it.number.contains("1009") }
        }
        if (payment?.payer?.contains("ТАТЬЯНА АНАТОЛЬЕВНА СОФРОНОВА", true) == true) {
            return accounts.find { it.number.contains("1072") }
        }
        return null
    }

}

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}

