package com.firelib.techbot

import chart.BreachType
import org.jetbrains.exposed.dao.id.IntIdTable

object BreachEvents : IntIdTable() {
    val ticker = varchar("ticker", 10)
    val timeframe = varchar("timeframe", 10)
    val photoFile = varchar("photo_file", 100)
    val eventTimeMs = long("event_time_ms")
    val eventType = varchar("event_type", 10).default(BreachType.TREND_LINE.name)

    init {
        index(false, eventTimeMs)
    }
}