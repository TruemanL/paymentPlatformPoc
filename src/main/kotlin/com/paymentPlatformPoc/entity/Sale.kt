package com.paymentPlatformPoc.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "sale_tbl")
data class Sale(
    @Column(nullable = false)
    val datetime: LocalDateTime,

    @Column(nullable = false, name = "transaction_price")
    val transactionPrice: BigDecimal,

    @Column(nullable = false)
    val points: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
)