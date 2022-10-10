package com.firelib.techbot.persistence

import org.jetbrains.exposed.dao.id.IntIdTable

object SignalTypes : IntIdTable() {
    val user = long("user_id")
    val signalType = varchar("signalType", 20)

    init {
        index(true, user, signalType)
    }
}