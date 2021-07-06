package ru.tsn.payment

import org.apache.commons.io.FilenameUtils
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import ru.tsn.payment.enums.SearchTypeEnum
import ru.tsn.payment.model.Account
import ru.tsn.payment.model.Payment
import ru.tsn.payment.parser.AccountParser
import ru.tsn.payment.parser.PaymentParser
import java.math.BigDecimal
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.Path
import kotlin.io.path.writeLines

@SpringBootApplication
class PaymentApplication : CommandLineRunner {

    private val DEFAULT_FOLDER = "etc"
//    private val FILE_NAME = "СберБизнес. Выписка за 2021.06.04-2021.06.17 счёт 40703810838000014811.xlsx"
    private val FILE_NAME = "СберБизнес. Выписка за 2021.06.18-2021.06.26 счёт 40703810838000014811.xlsx"

    override fun run(vararg args: String?) {
//        CarParser().parse("etc/МКЗ Транспорт ЭНЕРГИЯ ЗВУКА КОНЦЕРТНЫЙ ЗАЛ.xlsx")
        val accounts = AccountParser().parse("etc/ЛС УО1.xlsx")
        val payments = PaymentParser().parse(Paths.get(DEFAULT_FOLDER).resolve(FILE_NAME).toString())
        val lines = determine(payments, accounts)
        println(lines)
        Path(Paths.get(DEFAULT_FOLDER).resolve(fileName(FILE_NAME)).toString()).writeLines(lines);
    }

    private fun fileName(prefix: String): String {
        val baseName = FilenameUtils.getBaseName(prefix)
        val formatterFileName = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val dateFormat = LocalDateTime.now().format(formatterFileName)
        return "$baseName [$dateFormat].csv"
    }


    fun determine(payments: List<Payment>, accounts: List<Account>): List<String> {
        val paymentsWithFilter = payments
            .filterNot { it.purpose.contains("ПО ПРИНЯТЫМ ПЛАТЕЖАМ") || it.payer.contains("ГЦЖС") }
        val lines = mutableListOf<String>()
        for (payment in paymentsWithFilter) {
            val (account, searchType) = findAccountOrNull(accounts, payment)
            println(payment)
            println(account)
            println("---")
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
        items.add(payment.payer?.replace("\n", " ")?.replace(";", ","))
        items.add(payment.purpose.replace(";", ","))
        return items.joinToString(";")
    }

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

        return Pair(null, null)
    }

    fun findByFullAccount(accounts: List<Account>, purpose: String?): Account? {
        return accounts.firstOrNull {
            purpose?.contains(it.number, ignoreCase = true) == true
        }
    }

    fun findByShortAccount(accounts: List<Account>, purpose: String?): Account? {
        return accounts.firstOrNull {
            purpose?.contains(it.number.trimStart('0'), ignoreCase = true) == true
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

    fun findByHardCodeOrNull(accounts: List<Account>, payment: Payment?): Account? {
        if (payment?.payer?.contains("ВИЖИЦКИЙ ВАЛЕРИЙ АЛЕКСЕЕВИЧ") == true) {
            return accounts.find { it.number.contains("1007") }
        }
        if (payment?.payer?.contains("ПЕСКОВА КСЕНИЯ СЕРГЕЕВНА") == true) {
            return accounts.find { it.number.contains("1014") }
        }
        if (payment?.payer?.contains("ДЫБОВ ДЕНИС АЛЕКСАНДРОВИЧ") == true) {
            return accounts.find { it.number.contains("1093") }
        }
        return null
    }

}

fun main(args: Array<String>) {
    runApplication<PaymentApplication>(*args)
}

