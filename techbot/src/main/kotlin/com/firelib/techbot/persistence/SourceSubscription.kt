package com.firelib.techbot.persistence

import org.jetbrains.exposed.dao.id.IntIdTable

object SourceSubscription : IntIdTable() {
    val user = long("user_id")
    val sourceId = varchar("source_id", 20)
    val sourceName = varchar("source", 20)

    init {
        index(true, user, sourceId, sourceName)
    }
}