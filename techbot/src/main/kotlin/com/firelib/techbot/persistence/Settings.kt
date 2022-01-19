package com.firelib.techbot.persistence

import org.jetbrains.exposed.sql.Table

object Settings : Table() {
    val user = Settings.integer("user_id")
    val name = varchar("name", 50)
    val value = varchar("settings", 500)

    override val primaryKey = PrimaryKey(user, name = "settings_pk")
}