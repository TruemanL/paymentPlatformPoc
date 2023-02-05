package com.paymentPlatformPoc.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object DateTimeUtil {

    fun getLocalDateTimeFromIsoInstantString(input: String): LocalDateTime {
        return try {
            val dateInstant = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(input))
            LocalDateTime.ofInstant(dateInstant, ZoneId.of(ZoneOffset.UTC.id))
        } catch (e: Exception) {
            throw IllegalArgumentException("Illegal datetime value of $input")
        }
    }

    fun LocalDateTime.toIsoInstantString(): String {
        return this.toInstant(ZoneOffset.UTC).toString()
    }
}