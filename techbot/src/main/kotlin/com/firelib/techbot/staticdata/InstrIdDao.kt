package com.firelib.techbot.staticdata

import com.firelib.techbot.updateDatabase
import firelib.core.domain.InstrId
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

object Instruments : Table() {
    val id = varchar("id", 20)
    val code = varchar("code", 20)
    val market = varchar("market", 10)
    val sourceName = varchar("source", 20)
    val payload = blob("payload")

    init {
        index(true, id, code, market)
    }
}

class InstrIdDao {
    fun loadAll(): List<InstrId> {
        return transaction {
            Instruments.selectAll().map {
                JsonHelper.fromJson<InstrId>(String(it[Instruments.payload].bytes))
            }
        }

    }

    fun addAll(instrId: List<InstrId>) {
        updateDatabase("insert instruments") {
            Instruments.batchReplace(instrId) {
                this[Instruments.id] = it.id
                this[Instruments.code] = it.code
                this[Instruments.sourceName] = it.source
                this[Instruments.market] = it.market
                this[Instruments.payload] = ExposedBlob(JsonHelper.toJsonBytes(it))
            }
        }.get()

    }
}