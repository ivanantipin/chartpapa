package com.firelib.techbot.persistence

import org.jetbrains.exposed.sql.Table

object ConfigService : Table() {
    val name = varchar("name", 50)
    val value = varchar("value", 100)
    override val primaryKey = PrimaryKey(name, name = "bot_config_pk")
}