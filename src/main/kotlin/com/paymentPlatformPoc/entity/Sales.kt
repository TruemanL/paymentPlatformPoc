package com.paymentPlatformPoc.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "sales_tbl")
class Sales(
    @Column(nullable = false)
    val datetime: LocalDateTime,

    @Column(nullable = false)
    val sales: BigDecimal,

    @Column(nullable = false)
    val points: Long,

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null
)