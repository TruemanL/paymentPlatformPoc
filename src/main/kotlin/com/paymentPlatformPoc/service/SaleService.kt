package com.paymentPlatformPoc.service

import arrow.core.Validated
import arrow.core.Validated.*
import com.paymentPlatformPoc.dto.Payment
import com.paymentPlatformPoc.entity.PaymentMethod
import com.paymentPlatformPoc.entity.Sale
import com.paymentPlatformPoc.repository.PaymentMethodRepository
import org.springframework.stereotype.Service
import com.paymentPlatformPoc.extension.roundRate
import com.paymentPlatformPoc.extension.roundPoints
import com.paymentPlatformPoc.extension.roundPrice

@Service
class SaleService(
    val paymentMethodRepository: PaymentMethodRepository
) {
    fun getPaymentMethodIfValid(method: String): Validated<String, PaymentMethod> {
        return paymentMethodRepository.getByMethod(method)?.let { Valid(it) }
            ?: Invalid("Payment requested for unrecognized payment method: $method")
    }

    fun getSaleIfValid(payment: Payment, paymentMethod: PaymentMethod): Validated<String, Sale> {
        if (payment.priceModifier < paymentMethod.minModifier) {
            return Invalid("Payment requested for ${payment.paymentMethod} and " +
                    "price modifier ${payment.priceModifier.roundRate()} below the " +
                    "minimum acceptable value of ${paymentMethod.minModifier.roundRate()}")
        }
        if (payment.priceModifier > paymentMethod.maxModifier) {
            return Invalid("Payment requested for ${payment.paymentMethod} and " +
                    "price modifier ${payment.priceModifier.roundRate()} above the " +
                    "maximum acceptable value of ${paymentMethod.maxModifier.roundRate()}")
        }
        val transactionPrice = payment.price.multiply(payment.priceModifier).roundPrice()
        val points = payment.price.multiply(paymentMethod.pointRate).roundPoints()
        return Valid(Sale(payment.dateTime, transactionPrice, points))
    }
}