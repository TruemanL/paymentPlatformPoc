package com.paymentPlatformPoc.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.math.BigDecimal
import java.time.LocalDateTime

@SpringBootTest
class SaleRepositoryTest @Autowired constructor(
    val sut: SaleRepository,
    val jdbcTemplate: JdbcTemplate
) {

    @BeforeEach
    fun setup() {
        jdbcTemplate.execute("delete from sale_tbl")
    }

    @Test
    fun `getByDatetimeBetween_returns an empty list when there are no records in given time period`() {
        val testStartTime = LocalDateTime.of(2023, 1, 1, 0, 0)
        val testEndTime = LocalDateTime.of(2023, 1, 2, 0, 0)
        assertThat((sut.getByDatetimeBetween(testStartTime, testEndTime))).isEmpty()
    }

    @Test
    fun `getByDatetimeBetween_returns a list of records in given time period`() {
        jdbcTemplate.update(
            """
                insert into sale_tbl
                (datetime,              transaction_price, points, id) values
                ('2023-01-01 00:00:00', 100.0,             5,      1),
                ('2023-01-02 23:59:59', 200.0,             6,      2),
                ('2023-01-03 00:00:00', 300.0,             7,      3),
                ('2023-01-04 00:00:00', 400.0,             8,      4),
                ('2023-01-04 00:00:01', 500.0,             9,      5),
                ('2023-01-05 00:00:00', 600.0,             10,     6)
            """.trimIndent()
        )

        val testStartTime = LocalDateTime.of(2023, 1, 3, 0, 0)
        val testEndTime = LocalDateTime.of(2023, 1, 4, 0, 0)
        val result = sut.getByDatetimeBetween(testStartTime, testEndTime)

        assertThat(result.size == 2)
        with(result[0]) {
            assertThat(datetime).isEqualTo(LocalDateTime.of(2023, 1, 3, 0, 0, 0))
            assertThat(transactionPrice).isEqualTo(BigDecimal(300).setScale(2))
            assertThat(points).isEqualTo(7)
            assertThat(id).isEqualTo(3)
        }
        with(result[1]) {
            assertThat(datetime).isEqualTo(LocalDateTime.of(2023, 1, 4, 0, 0, 0))
            assertThat(transactionPrice).isEqualTo(BigDecimal(400).setScale(2))
            assertThat(points).isEqualTo(8)
            assertThat(id).isEqualTo(4)
        }
    }
}