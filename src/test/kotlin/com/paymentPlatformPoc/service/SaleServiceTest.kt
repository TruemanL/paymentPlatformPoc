package com.paymentPlatformPoc.service

import arrow.core.Validated
import com.paymentPlatformPoc.dto.PaymentDto
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
class SaleServiceTest @Autowired constructor(
    val sut: SaleService,
) {

    @Test
    fun `getSaleIfValid_returns expected error if payment method doesn't exist`() {
        val dummyPrice = BigDecimal.TEN
        val dummyPriceModifier = BigDecimal.ONE
        val dummyDateTime = LocalDateTime.now()
        val invalidPaymentMethod = "Invalid Method"
        val testPaymentDto = PaymentDto(dummyPrice, dummyPriceModifier, invalidPaymentMethod, dummyDateTime)

        assertEquals(
            Validated.Invalid("Payment requested for unrecognized payment method: Invalid Method"),
            sut.getSaleIfValid(testPaymentDto)
        )
    }

    @Test
    fun `getSaleIfValid_returns expected error if price modifier is less than minimum acceptable price modifier`() {
        val dummyPrice = BigDecimal.TEN
        val dummyDateTime = LocalDateTime.now()

        val paymentMethodName = "CASH" // minModifier = 0.9
        val priceModifier = BigDecimal(0.89)
        val testPaymentDto = PaymentDto(dummyPrice, priceModifier, paymentMethodName, dummyDateTime)

        assertEquals(
            Validated.Invalid("Payment requested for CASH and price modifier 0.89 below the minimum acceptable value of 0.90"),
            sut.getSaleIfValid(testPaymentDto)
        )
    }

    @Test
    fun `getSaleIfValid_returns expected error if price modifier is greater than maximum acceptable price modifier`() {
        val dummyPrice = BigDecimal.TEN
        val dummyDateTime = LocalDateTime.now()

        val paymentMethodName = "CASH_ON_DELIVERY" // maxModifier = 1.02
        val priceModifier = BigDecimal(1.03)
        val testPaymentDto = PaymentDto(dummyPrice, priceModifier, paymentMethodName, dummyDateTime)

        assertEquals(
            Validated.Invalid("Payment requested for CASH_ON_DELIVERY and price modifier 1.03 above the maximum acceptable value of 1.02"),
            sut.getSaleIfValid(testPaymentDto)
        )
    }

    @Test
    fun `getSaleIfValid_returns expected Sale if payment is valid`() {
        val paymentMethodName = "VISA" // minModifier = 0.95, maxModifier = 1, pointRate = 0.03

        val testPrice = BigDecimal(100)
        val testDateTime = LocalDateTime.now()
        val priceModifier = BigDecimal(0.99)
        val testPaymentDto = PaymentDto(testPrice, priceModifier, paymentMethodName, testDateTime)

        val result = sut.getSaleIfValid(testPaymentDto)

        result.fold(
            { invalid -> fail("the result was invalid")},
            { sale -> {
                assertEquals(testDateTime, sale.datetime)
                assertEquals(BigDecimal(99), sale.transactionPrice)
                assertEquals(3L, sale.points)
            }}
        )
    }
}