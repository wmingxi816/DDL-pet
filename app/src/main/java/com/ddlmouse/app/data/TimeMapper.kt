package com.ddlmouse.app.data

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object TimeMapper {
    private val zone: ZoneId = ZoneId.systemDefault()

    fun toEpochMillis(value: LocalDateTime?): Long? {
        return value?.atZone(zone)?.toInstant()?.toEpochMilli()
    }

    fun fromEpochMillis(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zone) }
    }

    fun requireEpochMillis(value: LocalDateTime): Long {
        return value.atZone(zone).toInstant().toEpochMilli()
    }
}

