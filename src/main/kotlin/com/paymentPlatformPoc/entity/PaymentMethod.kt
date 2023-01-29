package com.paymentPlatformPoc.entity

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "payment_method_mst")
data class PaymentMethod(
    @Id
    @Column(nullable = false)
    val method: String,

    @Column(nullable = false, name = "min_modifier")
    val minModifier: BigDecimal,

    @Column(nullable = false, name = "max_modifier")
    val maxModifier: BigDecimal,

    @Column(nullable = false, name = "point_rate")
    val pointRate: BigDecimal
)