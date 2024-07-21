package com.firelib.techbot.command

import org.jetbrains.exposed.sql.Table

object CacheTable : Table() {
    val entryKey = varchar("entry_key", 20)
    val payload = blob("payload")
    val expiryTime = long("expiry_time")
    val time = long("time_ms")
    override val primaryKey = PrimaryKey(CacheTable.entryKey, name = "cache_pk")
}