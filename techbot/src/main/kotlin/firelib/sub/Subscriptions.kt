package com.firelib.sub

import firelib.telbot.TimeFrame
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


fun main(args: Array<String>) {
    //an example connection to H2 DB
    Database.connect("jdbc:sqlite:/tmp/chatbot.db", driver = "org.sqlite.JDBC")

    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.drop(Subscriptions)
    }
}


object Users : IntIdTable() {
    val name = varchar("name", 50)
}

object Subscriptions: IntIdTable() {
    val user = integer("user_id")
    val ticker = varchar("ticker", 10)
    val timeframe = varchar("timeframe", 10).default(TimeFrame.H.name)

    init {
        index(true, user, ticker, timeframe)
    }
}

object BreachEvents : IntIdTable() {
    val ticker = varchar("ticker", 10)
    val timeframe = varchar("timeframe", 10)
    val photoFile = varchar("photo_file", 100)
    val eventTimeMs = long("event_time_ms")

    init {
        index(false, eventTimeMs)
    }


}

object UserNotifications : IntIdTable() {
    val subscriptionId = integer("subs_id")
}