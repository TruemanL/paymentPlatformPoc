package com.paymentPlatformPoc.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime

@SpringBootTest
class SalesRepositoryTest @Autowired constructor(
    val sut: SalesRepository
) {

    @Test
    fun `getByDatetimeBetween_returns an empty list when there are no records in given time period`() {
        val testStartTime = LocalDateTime.of(2023, 1, 1, 0, 0)
        val testEndTime = LocalDateTime.of(2023, 1, 2, 0, 0)
        assertThat((sut.getByDatetimeBetween(testStartTime, testEndTime))).isEmpty()
    }

}