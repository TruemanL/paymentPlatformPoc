package com.paymentPlatformPoc.repository

import com.paymentPlatformPoc.entity.PaymentMethod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PaymentMethodRepository: JpaRepository<PaymentMethod, String> {
    @Query("select p from PaymentMethod p where p.method = :method")
    fun getByMethod(@Param("method") method: String): PaymentMethod?
}