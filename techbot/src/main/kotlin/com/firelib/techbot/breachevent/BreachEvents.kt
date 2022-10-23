package com.firelib.techbot.breachevent

import com.firelib.techbot.SignalType
import com.firelib.techbot.domain.TimeFrame
import firelib.core.store.GlobalConstants
import org.jetbrains.exposed.dao.id.IntIdTable

object BreachEvents : IntIdTable() {
    val instrId = varchar("ticker", 30)
    val timeframe = varchar("timeframe", 10)
    val eventTimeMs = long("event_time_ms")
    val eventType = varchar("event_type", 30).default(SignalType.TREND_LINE.name)

    init {
        index(false, eventTimeMs)
    }

    fun makeSnapFileName(prefix: String, ticker: String, timeFrame: TimeFrame, eventTimeMs: Long): String {
        val fileName = "${prefix}_${ticker}_${timeFrame}_$eventTimeMs"
        return GlobalConstants.imgFolder.resolve("${fileName}.png").toFile().absoluteFile.toString()
    }

}



