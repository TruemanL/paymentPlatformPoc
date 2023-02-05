package com.paymentPlatformPoc.adaptor

import com.paymentPlatformPoc.exception.InputOutOfRangeException
import com.paymentPlatformPoc.grpc.adaptor.*
import com.paymentPlatformPoc.util.DateTimeUtil.toIsoInstantString
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.sql.Timestamp

@SpringBootTest
class PaymentAdaptorImplTest @Autowired constructor(
    val sut: PaymentAdaptorImpl,
    val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setUp() {
        jdbcTemplate.execute("delete from sale_tbl")
    }

    @Test
    fun `makePayment_throws IllegalArgumentException when PaymentRequest contains unrecognized payment method type`() {
        val illegalPaymentRequest = PaymentRequest.newBuilder()
            .setPrice("1.00")
            .setPriceModifier(1.00)
            .setPaymentMethod("Invalid Method")
            .setDatetime("2023-01-02T01:00:00Z")
            .build()

        val thrown = assertThrowsExactly(IllegalArgumentException::class.java) {
            runBlocking{ sut.makePayment(illegalPaymentRequest) }
        }
        assertEquals("Payment requested for unrecognized payment method: Invalid Method", thrown.message)
    }

    @Test
    fun `makePayment_throws InputOutOfRangeException when PaymentRequest contains unacceptable priceModifier`() {
        val illegalPaymentRequest = PaymentRequest.newBuilder()
            .setPrice("10.00")
            .setPriceModifier(0.99)
            .setPaymentMethod("CASH_ON_DELIVERY")
            .setDatetime("2023-01-02T01:00:00Z")
            .build()

        val thrown = assertThrowsExactly(InputOutOfRangeException::class.java) {
            runBlocking{ sut.makePayment(illegalPaymentRequest) }
        }
        assertEquals("Payment requested for CASH_ON_DELIVERY and price modifier 0.99 below the minimum acceptable value of 1.00",
            thrown.message)
    }

    @Test
    fun `makePayment_saves payment and returns PaymentResponse when PaymentRequest is valid`() {
        val paymentRequest = PaymentRequest.newBuilder()
            .setPrice("100.00")
            .setPriceModifier(1.01)
            .setPaymentMethod("CASH_ON_DELIVERY")
            .setDatetime("2023-01-02T01:00:00Z")
            .build()

        val expectedResponse = PaymentResponse.newBuilder()
            .setFinalPrice("101.00")
            .setPoints(5L)
            .build()

        runBlocking {
            assertEquals(expectedResponse, sut.makePayment(paymentRequest))
        }

        val sales = jdbcTemplate.queryForList("select * from sale_tbl")
        assertEquals(1, sales.size)
        with(sales[0]) {
            assertEquals(
                "2023-01-02T01:00:00Z",
                (this.getValue("datetime") as Timestamp).toLocalDateTime().toIsoInstantString()
            )
            assertEquals(BigDecimal(101).setScale(2), this.getValue("transaction_price"))
            assertEquals(5L, this.getValue("points"))
        }
    }

    @Test
    fun `getListOfSalesSummary_throws IllegalArgumentException when DateRange contains an invalid IsoInstant datetime`() {
        val invalidDateTimeString = "2022-09-01T00:00:00"
        val validDateTimeString = "2022-09-02T00:00:00Z"
        val illegalRequest = DateRange.newBuilder()
            .setStartDateTime(invalidDateTimeString)
            .setEndDateTime(validDateTimeString)
            .build()

        val thrown = assertThrowsExactly(IllegalArgumentException::class.java) {
            runBlocking{ sut.getListOfSalesSummary(illegalRequest) }
        }
        assertEquals("Illegal datetime value of 2022-09-01T00:00:00", thrown.message)
    }

    @Test
    fun `getListOfSalesSummary_returns expected SalesSummary`() {
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
        val startDateTimeString = "2023-01-02T00:00:00Z"
        val endDateTimeString = "2023-01-02T01:59:59Z"

        val testRequest = DateRange.newBuilder()
            .setStartDateTime(startDateTimeString)
            .setEndDateTime(endDateTimeString)
            .build()

        val expectedSalesSummary1 = SalesSummary.newBuilder()
            .setDatetime("2023-01-02T00:00:00Z")
            .setSales("2000.00")
            .setPoints(40L)
            .build()

        val expectedSalesSummary2 = SalesSummary.newBuilder()
            .setDatetime("2023-01-02T01:00:00Z")
            .setSales("1500.00")
            .setPoints(23L)
            .build()

        val expectedListOfSalesSummary = ListOfSalesSummary.newBuilder()
            .addSales(expectedSalesSummary1)
            .addSales(expectedSalesSummary2)
            .build()

        runBlocking{
            assertEquals(expectedListOfSalesSummary, sut.getListOfSalesSummary(testRequest))
        }
    }
}