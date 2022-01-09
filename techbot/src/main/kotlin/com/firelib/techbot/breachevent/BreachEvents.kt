package com.firelib.techbot.breachevent

import chart.BreachType
import com.firelib.techbot.domain.TimeFrame
import firelib.core.store.GlobalConstants
import org.jetbrains.exposed.dao.id.IntIdTable

object BreachEvents : IntIdTable() {
    val instrId = varchar("ticker", 10)
    val timeframe = varchar("timeframe", 10)
    val photoFile = varchar("photo_file", 100)
    val eventTimeMs = long("event_time_ms")
    val eventType = varchar("event_type", 30).default(BreachType.TREND_LINE.name)

    init {
        index(false, eventTimeMs)
    }

    fun makeSnapFileName(prefix: String, ticker: String, timeFrame: TimeFrame, eventTimeMs: Long): String {
        val fileName = "${prefix}_${ticker}_${timeFrame}_$eventTimeMs"
        return GlobalConstants.imgFolder.resolve("${fileName}.png").toFile().absoluteFile.toString()
    }

}



