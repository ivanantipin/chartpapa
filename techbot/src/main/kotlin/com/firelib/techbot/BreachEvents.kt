package com.firelib.techbot

import org.jetbrains.exposed.dao.id.IntIdTable

object BreachEvents : IntIdTable() {
    val ticker = varchar("ticker", 10)
    val timeframe = varchar("timeframe", 10)
    val photoFile = varchar("photo_file", 100)
    val eventTimeMs = long("event_time_ms")

    init {
        index(false, eventTimeMs)
    }
}