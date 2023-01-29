package com.paymentPlatformPoc.repository

import com.paymentPlatformPoc.entity.Sale
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SaleRepository: JpaRepository<Sale, Long> {

    @Query(
        "select * from sale_tbl s where s.datetime >= :startDateTime and s.datetime <= :endDateTime",
        nativeQuery = true
    )
    fun getByDatetimeBetween(
        @Param("startDateTime") startDateTime: LocalDateTime,
        @Param("endDateTime") endDateTime: LocalDateTime
    ): List<Sale>

}