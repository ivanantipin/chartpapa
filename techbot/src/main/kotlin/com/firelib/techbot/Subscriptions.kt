package com.firelib.techbot

import org.jetbrains.exposed.dao.id.IntIdTable

object Subscriptions : IntIdTable() {
    val user = integer("user_id")
    val ticker = varchar("ticker", 10)

    init {
        index(true, user, ticker)
    }
}

object TimeFrames : IntIdTable() {
    val user = integer("user_id")
    val tf = varchar("tf", 10)

    init {
        index(true, user, tf)
    }
}

