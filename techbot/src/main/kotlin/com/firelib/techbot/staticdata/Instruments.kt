package com.firelib.techbot.staticdata

import org.jetbrains.exposed.sql.Table

object Instruments : Table() {
    val id = varchar("id", 1000)
    val code = varchar("code", 100)
    val market = varchar("market", 1000)
    val sourceName = varchar("source", 2000)
    val payload = blob("payload")

    init {
        index(true, id, code, market)
    }
}