package com.paymentPlatformPoc.adaptor

import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.dto.SalesDto
import com.paymentPlatformPoc.entity.Sale
import com.paymentPlatformPoc.grpc.adaptor.*
import com.paymentPlatformPoc.service.SaleService
import com.paymentPlatformPoc.util.DateTimeUtil
import net.devh.boot.grpc.server.service.GrpcService
import java.math.BigDecimal

@GrpcService
class PaymentAdaptorImpl(val saleService: SaleService): PaymentAdaptorGrpcKt.PaymentAdaptorCoroutineImplBase() {
    override suspend fun makePayment(request: PaymentRequest): PaymentResponse {
        return saleService.getSaleIfValid(request.toPaymentDto())
            .fold(
                { errorMessage -> throw Exception(errorMessage) },
                { sale -> {
                    saleService.save(sale)
                    sale.toPaymentResponse()
                } }
            ).invoke()
    }

    private fun PaymentRequest.toPaymentDto(): PaymentDto =
        PaymentDto(
            price = BigDecimal(price),
            priceModifier = BigDecimal(priceModifier),
            paymentMethod = paymentMethod,
            dateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(datetime) ?: throw Exception()
        )

    private fun Sale.toPaymentResponse(): PaymentResponse =
        PaymentResponse.newBuilder()
            .setFinalPrice(transactionPrice.toString())
            .setPoints(points)
            .build()


    override suspend fun getListOfSalesSummary(request: DateRange): ListOfSalesSummary {
        val startDateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(request.startDateTime)
            ?: throw Exception()
        val endDateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(request.endDateTime)
            ?: throw Exception()
        val salesSummary = saleService.getSalesDtoListInRange(startDateTime, endDateTime).map { it.toSalesSummary() }
        return ListOfSalesSummary.newBuilder().addAllSales(salesSummary).build()
    }

    private fun SalesDto.toSalesSummary(): SalesSummary =
        SalesSummary.newBuilder()
            .setDatetime(datetime)
            .setSales(sales)
            .setPoints(points)
            .build()
}