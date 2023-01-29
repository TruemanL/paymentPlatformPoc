package com.paymentPlatformPoc.service

import arrow.core.Validated
import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.entity.PaymentMethod
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime

@SpringBootTest
class SaleServiceTest @Autowired constructor(
    val sut: SaleService,
    val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("delete from payment_method_mst")
    }

    @Test
    fun `getPaymentMethodIfValid_returns expected payment method if payment method exists`() {
        jdbcTemplate.update(
            """
                insert into payment_method_mst
                (method, min_modifier, max_modifier, point_rate) values
                ('CASH', 0.9,          1,            0.05),
                ('AMEX', 0.98,         1.01,         0.02)
            """.trimIndent()
        )
        assertEquals(
            Validated.Valid(
                PaymentMethod(
                    "AMEX",
                    BigDecimal(0.98).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal(1.01).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal(0.02).setScale(2, RoundingMode.HALF_UP)
                )
            ),
            sut.getPaymentMethodIfValid("AMEX")
        )
    }

    @Test
    fun `getPaymentMethodIfValid_returns expected error if payment method doesn't exist`() {
        assertEquals(
            Validated.Invalid("Payment requested for unrecognized payment method: invalid method"),
            sut.getPaymentMethodIfValid("invalid method")
        )
    }

    @Test
    fun `getSaleIfValid_returns expected error if price modifier is less than minimum acceptable price modifier`() {
        val dummyPrice = BigDecimal.TEN
        val dummyDateTime = LocalDateTime.now()
        val dummyPointRate = BigDecimal.ZERO

        val paymentMethodName = "paymentMethod"
        val priceModifier = BigDecimal(0.97)
        val testPaymentDto = PaymentDto(dummyPrice, priceModifier, paymentMethodName, dummyDateTime)

        val minModifier = BigDecimal(0.98)
        val maxModifier = BigDecimal(1.01)
        val testPaymentMethod = PaymentMethod(paymentMethodName, minModifier, maxModifier, dummyPointRate)

        assertEquals(
            Validated.Invalid("Payment requested for paymentMethod and price modifier 0.97 below the minimum acceptable value of 0.98"),
            sut.getSaleIfValid(testPaymentDto, testPaymentMethod)
        )
    }

    @Test
    fun `getSaleIfValid_returns expected error if price modifier is greater than maximum acceptable price modifier`() {
        val dummyPrice = BigDecimal.TEN
        val dummyDateTime = LocalDateTime.now()
        val dummyPointRate = BigDecimal.ZERO

        val paymentMethodName = "paymentMethod"
        val priceModifier = BigDecimal(1.02)
        val testPaymentDto = PaymentDto(dummyPrice, priceModifier, paymentMethodName, dummyDateTime)

        val minModifier = BigDecimal(0.98)
        val maxModifier = BigDecimal(1.01)
        val testPaymentMethod = PaymentMethod(paymentMethodName, minModifier, maxModifier, dummyPointRate)

        assertEquals(
            Validated.Invalid("Payment requested for paymentMethod and price modifier 1.02 above the maximum acceptable value of 1.01"),
            sut.getSaleIfValid(testPaymentDto, testPaymentMethod)
        )
    }

    @Test
    fun `getSaleIfValid_returns expected Sale if payment is valid`() {
        val paymentMethodName = "paymentMethod"

        val testPrice = BigDecimal(100)
        val testDateTime = LocalDateTime.now()
        val priceModifier = BigDecimal(0.99)
        val testPaymentDto = PaymentDto(testPrice, priceModifier, paymentMethodName, testDateTime)

        val minModifier = BigDecimal(0.98)
        val maxModifier = BigDecimal(1.01)
        val testPointRate = BigDecimal(0.02)
        val testPaymentMethod = PaymentMethod(paymentMethodName, minModifier, maxModifier, testPointRate)

        val result = sut.getSaleIfValid(testPaymentDto, testPaymentMethod)

        result.fold(
            { invalid -> fail("the result was invalid")},
            { sale -> {
                assertEquals(testDateTime, sale.datetime)
                assertEquals(BigDecimal(99), sale.transactionPrice)
                assertEquals(2L, sale.points)
            }}
        )
    }
}