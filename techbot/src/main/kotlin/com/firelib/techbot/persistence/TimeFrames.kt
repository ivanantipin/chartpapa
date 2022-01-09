package com.firelib.techbot.persistence

import org.jetbrains.exposed.dao.id.IntIdTable

object TimeFrames : IntIdTable() {
    val user = integer("user_id")
    val tf = varchar("tf", 10)

    init {
        index(true, user, tf)
    }
}