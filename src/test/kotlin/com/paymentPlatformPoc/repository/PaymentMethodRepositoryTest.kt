package com.paymentPlatformPoc.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.math.RoundingMode

@SpringBootTest
class PaymentMethodRepositoryTest @Autowired constructor(
    val sut: PaymentMethodRepository,
    val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("delete from payment_method_mst")
    }

    @Test
    fun `getByMethod_returns null if no payment method matches input method name`() {
        assertThat((sut.getByMethod("nonexistent method"))).isNull()
    }

    @Test
    fun `getByMethod_returns expected payment method when a payment method exists that matches input method name`() {
        jdbcTemplate.update(
            """
                insert into payment_method_mst
                (method, min_modifier, max_modifier, point_rate) values
                ('CASH', 0.9,          1,            0.05),
                ('AMEX', 0.98,         1.01,         0.02)
            """.trimIndent()
        )

        val result = sut.getByMethod("CASH")

        assertThat(result).isNotNull
        result?.let {
            assertThat(it.method).isEqualTo("CASH")
            assertThat(it.minModifier).isEqualTo(BigDecimal(0.9).setScale(2, RoundingMode.HALF_UP))
            assertThat(it.maxModifier).isEqualTo(BigDecimal(1).setScale(2, RoundingMode.HALF_UP))
            assertThat(it.pointRate).isEqualTo(BigDecimal(0.05).setScale(2, RoundingMode.HALF_UP))
        }
    }
}