package com.paymentPlatformPoc.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class SalesDto(
    val datetime: LocalDateTime,
    val sales: BigDecimal,
    val points: Long,
)
