package com.firelib.techbot.staticdata

import org.jetbrains.exposed.sql.Table

object Instruments : Table() {
    val id = varchar("id", 20)
    val code = varchar("code", 20)
    val market = varchar("market", 10)
    val sourceName = varchar("source", 20)
    val payload = blob("payload")

    init {
        index(true, id, code, market)
    }
}