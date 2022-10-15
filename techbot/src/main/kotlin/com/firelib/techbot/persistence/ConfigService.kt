package com.firelib.techbot.persistence

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ConfigService : Table("ConfigService") {
    val name = varchar("name", 50)
    val value = varchar("value", 100)
    override val primaryKey = PrimaryKey(name, name = "bot_config_pk")

    fun initSystemVars() {
        transaction {
            ConfigService.selectAll().forEach {
                System.setProperty(it.get(name), it.get(value))
            }
        }
    }
}

