package com.paymentPlatformPoc.service

import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.dto.SalesDto
import com.paymentPlatformPoc.exception.InputOutOfRangeException
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
class SaleServiceTest @Autowired constructor(
    val sut: SaleService,
    val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("delete from sale_tbl")
    }

    @Test
    fun `getSaleIfValid_returns expected error if payment method doesn't exist`() {
        val dummyPrice = BigDecimal.TEN
        val dummyPriceModifier = BigDecimal.ONE
        val dummyDateTime = LocalDateTime.now()
        val invalidPaymentMethod = "Invalid Method"
        val testPaymentDto = PaymentDto(dummyPrice, dummyPriceModifier, invalidPaymentMethod, dummyDateTime)

        val thrown = assertThrowsExactly(IllegalArgumentException::class.java) {
            sut.getSale(testPaymentDto)
        }
        assertEquals("Payment requested for unrecognized payment method: Invalid Method", thrown.message)
    }

    @Test
    fun `getSaleIfValid_returns expected error if price modifier is less than minimum acceptable price modifier`() {
        val dummyPrice = BigDecimal.TEN
        val dummyDateTime = LocalDateTime.now()

        val paymentMethodName = "CASH" // minModifier = 0.9
        val priceModifier = BigDecimal(0.89)
        val testPaymentDto = PaymentDto(dummyPrice, priceModifier, paymentMethodName, dummyDateTime)

        val thrown = assertThrowsExactly(InputOutOfRangeException::class.java) {
            sut.getSale(testPaymentDto)
        }
        assertEquals("Payment requested for CASH and price modifier 0.89 below the minimum acceptable value of 0.90", thrown.message)
    }

    @Test
    fun `getSaleIfValid_returns expected error if price modifier is greater than maximum acceptable price modifier`() {
        val dummyPrice = BigDecimal.TEN
        val dummyDateTime = LocalDateTime.now()

        val paymentMethodName = "CASH_ON_DELIVERY" // maxModifier = 1.02
        val priceModifier = BigDecimal(1.03)
        val testPaymentDto = PaymentDto(dummyPrice, priceModifier, paymentMethodName, dummyDateTime)

        val thrown = assertThrowsExactly(InputOutOfRangeException::class.java) {
            sut.getSale(testPaymentDto)
        }
        assertEquals("Payment requested for CASH_ON_DELIVERY and price modifier 1.03 above the maximum acceptable value of 1.02", thrown.message)
    }

    @Test
    fun `getSaleIfValid_returns expected Sale if payment is valid`() {
        val paymentMethodName = "VISA" // minModifier = 0.95, maxModifier = 1, pointRate = 0.03

        val testPrice = BigDecimal(100)
        val testDateTime = LocalDateTime.now()
        val priceModifier = BigDecimal(0.99)
        val testPaymentDto = PaymentDto(testPrice, priceModifier, paymentMethodName, testDateTime)

        val result = sut.getSale(testPaymentDto)
        assertEquals(testDateTime, result.datetime)
        assertEquals(BigDecimal(99).setScale(2), result.transactionPrice)
        assertEquals(3L, result.points)
    }

    @Test
    fun `getSalesDtoListInRange_returns expected SalesDtos`() {
        jdbcTemplate.update(
            """
                insert into sale_tbl
                (datetime,              transaction_price, points, id) values
                ('2023-01-01 23:59:59', 100.00,            5,      1),
                ('2023-01-02 00:00:00', 200.00,            6,      2),
                ('2023-01-02 00:29:39', 300.00,            7,      3),
                ('2023-01-02 00:30:00', 400.00,            8,      4),
                ('2023-01-02 00:30:01', 500.00,            9,      5),
                ('2023-01-02 00:59:59', 600.00,            10,     6),
                ('2023-01-02 01:00:00', 700.00,            11,     7),
                ('2023-01-02 01:59:59', 800.00,            12,     8),
                ('2023-01-02 02:00:00', 900.00,            13,     9),
                ('2023-01-03 00:00:00', 1000.00,           14,     10)
            """.trimIndent()
        )

        val testStartTime = LocalDateTime.of(2023, 1, 2, 0, 0, 0)
        val testEndTime = LocalDateTime.of(2023, 1, 2, 1, 59, 59)

        val result = sut.getSalesDtoListInRange(testStartTime, testEndTime)

        assertEquals(
            listOf(
                SalesDto(LocalDateTime.of(2023, 1, 2, 0, 0, 0),
                    BigDecimal(2000).setScale(2),
                    40L),
                SalesDto(LocalDateTime.of(2023, 1, 2, 1, 0, 0),
                    BigDecimal(1500).setScale(2),
                    23L)
            ),
            result
        )
    }
}