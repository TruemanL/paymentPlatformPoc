package com.paymentPlatformPoc.repository

import com.paymentPlatformPoc.entity.Sales
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface SalesRepository: JpaRepository<Sales, Long> {

    @Query(
        "select * from sales_tbl s where s.datetime >= :startDateTime and s.datetime <= :endDateTime",
        nativeQuery = true
    )
    fun getByDatetimeBetween(
        @Param("startDateTime") startDateTime: LocalDateTime,
        @Param("endDateTime") endDateTime: LocalDateTime
    ): List<Sales>

}