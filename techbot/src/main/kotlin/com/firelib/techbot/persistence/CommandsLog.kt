package com.firelib.techbot.persistence

import org.jetbrains.exposed.dao.id.IntIdTable

object CommandsLog : IntIdTable() {
    val user = integer("user_id")
    val cmd = varchar("cmd", 300)
    val timestamp = long("timestamp_ms")
}