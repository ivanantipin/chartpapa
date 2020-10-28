package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import org.jetbrains.exposed.dao.id.IntIdTable

object Subscriptions : IntIdTable() {
    val user = integer("user_id")
    val ticker = varchar("ticker", 10)
    val timeframe = varchar("timeframe", 10).default(TimeFrame.H.name)

    init {
        index(true, user, ticker, timeframe)
    }
}