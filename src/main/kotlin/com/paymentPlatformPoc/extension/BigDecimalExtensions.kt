package com.paymentPlatformPoc.extension

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.roundPoints(): Long {
    return this.round(0).longValueExact()
}

fun BigDecimal.roundPrice(): BigDecimal {
    return this.round(2)
}

fun BigDecimal.roundRate(): BigDecimal {
    return this.round(2)
}

fun BigDecimal.round(scale: Int): BigDecimal {
    return this.setScale(scale, RoundingMode.HALF_UP)
}