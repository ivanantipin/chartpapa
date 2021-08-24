package com.firelib.techbot.command

import org.jetbrains.exposed.sql.Table

object CacheTable : Table() {
    val key = varchar("key", 20)
    val payload = blob("payload")
    val expiryTime = long("expiryTime")
    override val primaryKey = PrimaryKey(CacheTable.key, name = "cache_pk")
}