package ru.tsn.payment.model

import java.math.BigDecimal

class Account(
    var number: String,
    var lastName: String?,
    var firstName: String?,
    var middleName: String?,
    var square: BigDecimal = BigDecimal.ZERO



) {
    override fun toString(): String {
        return "Account(number='$number', lastName=$lastName, firstName=$firstName, middleName=$middleName, square=$square)"
    }
}