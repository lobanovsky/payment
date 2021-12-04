package ru.tsn.payment.model

import java.math.BigDecimal
import java.time.LocalDateTime

class Payment(
    var uuid: String,
    var date: LocalDateTime,
    var payer: String,
    var sum: BigDecimal = BigDecimal.ZERO,
    var docNumber: String,
    var purpose: String
){

    override fun toString(): String {
        return "Payment(uuid='$uuid', date=$date, payer='$payer', sum=$sum, docNumber='$docNumber', purpose='$purpose')"
    }
}

