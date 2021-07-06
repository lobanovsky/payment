package ru.tsn.payment.model

import java.math.BigDecimal
import java.time.LocalDateTime

class Payment(
    var id: String,
    var date: LocalDateTime,
    var payer: String,
    var sum: BigDecimal = BigDecimal.ZERO,
    var purpose: String
){
    override fun toString(): String {
        return "Payment(id='$id', date=$date, payer='$payer', sum=$sum, purpose='$purpose')"
    }
}

