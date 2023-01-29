package com.paymentPlatformPoc.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class Payment(
    val price: BigDecimal,
    val priceModifier: BigDecimal,
    val paymentMethod: String,
    val dateTime: LocalDateTime
)
