package com.firelib.techbot.staticdata

import com.firelib.techbot.persistence.DbHelper
import firelib.core.domain.InstrId
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

class InstrIdDao {
    fun loadAll(): List<InstrId> {
        return transaction {
            Instruments.selectAll().map {
                JsonHelper.fromJson<InstrId>(String(it[Instruments.payload].bytes))
            }
        }

    }

    fun addAll(instrId: List<InstrId>) {
        DbHelper.updateDatabase("insert instruments") {
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