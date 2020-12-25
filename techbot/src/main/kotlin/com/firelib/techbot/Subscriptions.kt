package com.firelib.techbot

import firelib.finam.FinamDownloader
import org.jetbrains.exposed.dao.id.IntIdTable

object Subscriptions : IntIdTable() {
    val user = integer("user_id")
    val ticker = varchar("ticker", 10)
    val market = varchar("market", 10).default(FinamDownloader.FinamMarket.SHARES_MARKET.id)

    init {
        index(true, user, ticker, market)
    }
}

object TimeFrames : IntIdTable() {
    val user = integer("user_id")
    val tf = varchar("tf", 10)

    init {
        index(true, user, tf)
    }
}

