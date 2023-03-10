package com.paymentPlatformPoc.util

import com.paymentPlatformPoc.util.DateTimeUtil.toIsoInstantString
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime

class DateTimeUtilTest {

    @Test
    fun `getLocalDateTimeFromIsoInstantString_throws IllegalArgumentException when input is not a valid IsoInstant datetime`() {
        val thrown = assertThrowsExactly(IllegalArgumentException::class.java) {
            DateTimeUtil.getLocalDateTimeFromIsoInstantString("2022-09-01T00:00:00")
        }
        assertEquals("Illegal datetime value of 2022-09-01T00:00:00", thrown.message)
    }

    @Test
    fun `getLocalDateTimeFromIsoInstantString_returns local date time of valid IsoInstant datetime input`() {
        assertEquals(
            LocalDateTime.of(2022, 9, 1, 0, 0, 0),
            DateTimeUtil.getLocalDateTimeFromIsoInstantString("2022-09-01T00:00:00Z")
        )
    }

    @Test
    fun `toIsoInstantString_returns string representation of localdatetime in IsoInstant format`() {
        assertEquals(
            "2022-09-01T00:00:00Z",
            LocalDateTime.of(2022, 9, 1, 0, 0, 0).toIsoInstantString()
        )
    }
}