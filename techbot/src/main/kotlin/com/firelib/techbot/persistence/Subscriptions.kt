package com.firelib.techbot.persistence

import firelib.finam.FinamDownloader
import org.jetbrains.exposed.dao.id.IntIdTable

object SourceSubscription : IntIdTable(){
    val user = long("user_id")
    val sourceId = varchar("ticker", 20)
    val sourceName = varchar("source", 20)

    init {
        index(true, user, sourceId, sourceName)
    }
}

object Subscriptions : IntIdTable() {
    val user = long("user_id")
    val ticker = varchar("ticker", 10)
    val market = varchar("market", 10).default(FinamDownloader.FinamMarket.SHARES_MARKET.id)

    init {
        index(true, user, ticker, market)
    }
}

