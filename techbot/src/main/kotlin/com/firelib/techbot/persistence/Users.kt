package com.firelib.techbot.persistence

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId = long("user_id")
    val name = varchar("name", 50)
    val familyName = varchar("family_name", 50)
}