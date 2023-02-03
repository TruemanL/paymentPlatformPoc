package com.paymentPlatformPoc.adaptor

import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.grpc.adaptor.*
import com.paymentPlatformPoc.service.SaleService
import com.paymentPlatformPoc.util.DateTimeUtil
import net.devh.boot.grpc.server.service.GrpcService
import java.math.BigDecimal

@GrpcService
class PaymentAdaptorImpl(val saleService: SaleService): PaymentAdaptorGrpcKt.PaymentAdaptorCoroutineImplBase() {
    override suspend fun makePayment(request: PaymentRequest): PaymentResponse {
        val paymentDto = getPaymentDto(request)
        return saleService.getSaleIfValid(paymentDto)
            .fold(
                { errorMessage -> throw Exception(errorMessage) },
                { sale -> {
                    saleService.save(sale)
                    PaymentResponse.newBuilder()
                        .setFinalPrice(sale.transactionPrice.toString())
                        .setPoints(sale.points)
                        .build()
                } }
            ).invoke()
    }

    override suspend fun getListOfSalesSummary(request: DateRange): ListOfSalesSummary {
        val startDateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(request.startDateTime)
            ?: throw Exception()
        val endDateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(request.endDateTime)
            ?: throw Exception()
        val salesSummary: List<SalesSummary> = saleService.getSalesDtoListInRange(startDateTime, endDateTime)
            .map {
                SalesSummary.newBuilder()
                    .setDatetime(it.datetime)
                    .setSales(it.sales)
                    .setPoints(it.points)
                    .build()
            }
        return ListOfSalesSummary.newBuilder().addAllSales(salesSummary).build()
    }

    private fun getPaymentDto(paymentRequest: PaymentRequest): PaymentDto {
        return with(paymentRequest) {
            PaymentDto(
                price = BigDecimal(price),
                priceModifier = BigDecimal(priceModifier),
                paymentMethod = paymentMethod,
                dateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(datetime) ?: throw Exception()
            )
        }
    }
}