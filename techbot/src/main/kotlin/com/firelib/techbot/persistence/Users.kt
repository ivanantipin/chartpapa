package com.firelib.techbot.persistence

import com.firelib.techbot.Langs
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId = long("user_id")
    val name = varchar("name", 50)
    val lang = varchar("lang", 10).default(Langs.RU.name)
    val familyName = varchar("family_name", 50)
}