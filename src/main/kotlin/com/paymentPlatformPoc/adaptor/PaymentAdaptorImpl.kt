package com.paymentPlatformPoc.adaptor

import com.paymentPlatformPoc.dto.PaymentDto
import com.paymentPlatformPoc.dto.SalesDto
import com.paymentPlatformPoc.entity.Sale
import com.paymentPlatformPoc.extension.roundPrice
import com.paymentPlatformPoc.grpc.adaptor.*
import com.paymentPlatformPoc.service.SaleService
import com.paymentPlatformPoc.util.DateTimeUtil
import com.paymentPlatformPoc.util.DateTimeUtil.toIsoInstantString
import net.devh.boot.grpc.server.service.GrpcService
import java.math.BigDecimal

@GrpcService
class PaymentAdaptorImpl(val saleService: SaleService): PaymentAdaptorGrpcKt.PaymentAdaptorCoroutineImplBase() {
    override suspend fun makePayment(request: PaymentRequest): PaymentResponse {
        val sale = saleService.getSale(request.toPaymentDto())
        saleService.save(sale)
        return sale.toPaymentResponse()
    }

    private fun PaymentRequest.toPaymentDto(): PaymentDto =
        PaymentDto(
            price = BigDecimal(price),
            priceModifier = BigDecimal(priceModifier),
            paymentMethod = paymentMethod,
            dateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(datetime)
        )

    private fun Sale.toPaymentResponse(): PaymentResponse =
        PaymentResponse.newBuilder()
            .setFinalPrice(transactionPrice.toString())
            .setPoints(points)
            .build()


    override suspend fun getListOfSalesSummary(request: DateRange): ListOfSalesSummary {
        val startDateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(request.startDateTime)
        val endDateTime = DateTimeUtil.getLocalDateTimeFromIsoInstantString(request.endDateTime)
        val salesSummary = saleService.getSalesDtoListInRange(startDateTime, endDateTime).map { it.toSalesSummary() }
        return ListOfSalesSummary.newBuilder().addAllSales(salesSummary).build()
    }

    private fun SalesDto.toSalesSummary(): SalesSummary =
        SalesSummary.newBuilder()
            .setDatetime(datetime.toIsoInstantString())
            .setSales(sales.roundPrice().toString())
            .setPoints(points)
            .build()
}