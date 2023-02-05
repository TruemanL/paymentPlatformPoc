package com.paymentPlatformPoc.service

import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.dto.SalesDto
import com.paymentPlatformPoc.entity.Sale
import com.paymentPlatformPoc.enums.PaymentMethodEnum
import com.paymentPlatformPoc.exception.InputOutOfRangeException
import org.springframework.stereotype.Service
import com.paymentPlatformPoc.extension.roundRate
import com.paymentPlatformPoc.extension.roundPoints
import com.paymentPlatformPoc.extension.roundPrice
import com.paymentPlatformPoc.repository.SaleRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class SaleService(
    val saleRepository: SaleRepository
) {

    fun getSale(paymentDto: PaymentDto): Sale {
        val paymentMethod: PaymentMethodEnum = PaymentMethodEnum.fromMethodName(paymentDto.paymentMethod)
            ?: throw IllegalArgumentException("Payment requested for unrecognized payment method: ${paymentDto.paymentMethod}")

        if (paymentDto.priceModifier < paymentMethod.minModifier) {
            throw InputOutOfRangeException("Payment requested for ${paymentDto.paymentMethod} and " +
                    "price modifier ${paymentDto.priceModifier.roundRate()} below the " +
                    "minimum acceptable value of ${paymentMethod.minModifier.roundRate()}")
        }
        if (paymentDto.priceModifier > paymentMethod.maxModifier) {
            throw InputOutOfRangeException("Payment requested for ${paymentDto.paymentMethod} and " +
                    "price modifier ${paymentDto.priceModifier.roundRate()} above the " +
                    "maximum acceptable value of ${paymentMethod.maxModifier.roundRate()}")
        }
        val transactionPrice = paymentDto.price.multiply(paymentDto.priceModifier).roundPrice()
        val points = paymentDto.price.multiply(paymentMethod.pointRate).roundPoints()
        return Sale(paymentDto.dateTime, transactionPrice, points)
    }

    fun save(sale: Sale) {
        saleRepository.save(sale)
    }

    fun getSalesDtoListInRange(startDateTime: LocalDateTime, endDateTime: LocalDateTime): List<SalesDto> {
        //TODO: validate startDateTime < endDateTime
        //TODO: keep original data types (move type conversion to adapter)
        val sales = saleRepository.getByDatetimeBetween(startDateTime, endDateTime)
        return sales.groupBy {
            it.datetime.truncatedTo(ChronoUnit.HOURS)
        }.map{
            val datetimeBucket = it.key

            val totalSalesInBucket = it.value.fold(BigDecimal.ZERO) { acc, sale ->
                acc + sale.transactionPrice
            }

            val totalPointsInBucket = it.value.sumOf { sale -> sale.points }

            SalesDto(datetimeBucket, totalSalesInBucket, totalPointsInBucket)
        }
    }
}