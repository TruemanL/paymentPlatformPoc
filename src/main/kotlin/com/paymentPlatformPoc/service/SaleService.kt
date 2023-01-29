package com.paymentPlatformPoc.service

import arrow.core.Validated
import arrow.core.Validated.*
import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.dto.SaleDto
import com.paymentPlatformPoc.entity.PaymentMethod
import com.paymentPlatformPoc.entity.Sale
import com.paymentPlatformPoc.repository.PaymentMethodRepository
import org.springframework.stereotype.Service
import com.paymentPlatformPoc.extension.roundRate
import com.paymentPlatformPoc.extension.roundPoints
import com.paymentPlatformPoc.extension.roundPrice
import com.paymentPlatformPoc.repository.SaleRepository
import com.paymentPlatformPoc.util.DateTimeUtil.toIsoInstantString
import java.time.LocalDateTime

@Service
class SaleService(
    val paymentMethodRepository: PaymentMethodRepository,
    val saleRepository: SaleRepository
) {
    fun getPaymentMethodIfValid(method: String): Validated<String, PaymentMethod> {
        return paymentMethodRepository.getByMethod(method)?.let { Valid(it) }
            ?: Invalid("Payment requested for unrecognized payment method: $method")
    }

    fun getSaleIfValid(paymentDto: PaymentDto, paymentMethod: PaymentMethod): Validated<String, Sale> {
        if (paymentDto.priceModifier < paymentMethod.minModifier) {
            return Invalid("Payment requested for ${paymentDto.paymentMethod} and " +
                    "price modifier ${paymentDto.priceModifier.roundRate()} below the " +
                    "minimum acceptable value of ${paymentMethod.minModifier.roundRate()}")
        }
        if (paymentDto.priceModifier > paymentMethod.maxModifier) {
            return Invalid("Payment requested for ${paymentDto.paymentMethod} and " +
                    "price modifier ${paymentDto.priceModifier.roundRate()} above the " +
                    "maximum acceptable value of ${paymentMethod.maxModifier.roundRate()}")
        }
        val transactionPrice = paymentDto.price.multiply(paymentDto.priceModifier).roundPrice()
        val points = paymentDto.price.multiply(paymentMethod.pointRate).roundPoints()
        return Valid(Sale(paymentDto.dateTime, transactionPrice, points))
    }

    //TODO: test after details are confirmed
    fun getSaleDtoListInRange(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<SaleDto> {
        val sales = saleRepository.getByDatetimeBetween(startDateTime, endDateTime)
        return sales.map{
            SaleDto(
                it.datetime.toIsoInstantString(),
                it.transactionPrice.roundPrice().toString(),
                it.points
            )
        }
    }
}