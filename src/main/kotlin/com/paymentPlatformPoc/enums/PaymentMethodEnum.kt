package com.paymentPlatformPoc.enums

import java.math.BigDecimal

enum class PaymentMethodEnum(
    val minModifier: BigDecimal,
    val maxModifier: BigDecimal,
    val pointRate: BigDecimal
) {
    CASH(BigDecimal(0.9), BigDecimal(1), BigDecimal(0.05)),
    CASH_ON_DELIVERY(BigDecimal(1), BigDecimal(1.02), BigDecimal(0.05)),
    VISA(BigDecimal(0.95), BigDecimal(1), BigDecimal(0.03)),
    MASTERCARD(BigDecimal(0.95), BigDecimal(1), BigDecimal(0.03)),
    AMEX(BigDecimal(0.98), BigDecimal(1.01), BigDecimal(0.02)),
    JCB(BigDecimal(0.95), BigDecimal(1), BigDecimal(0.05));

    companion object {
        fun fromMethodName(methodName: String): PaymentMethodEnum? {
            return PaymentMethodEnum.values().find{ it.name == methodName}
        }
    }
}