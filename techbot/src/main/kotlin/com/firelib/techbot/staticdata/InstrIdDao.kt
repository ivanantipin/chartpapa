package com.firelib.techbot.staticdata

import com.firelib.techbot.persistence.DbHelper
import firelib.core.domain.InstrId
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.batchReplace
import org.jetbrains.exposed.sql.deleteWhere
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

    suspend fun deleteById(instrId: String){
        DbHelper.updateDatabase("delete instrument") {
            Instruments.deleteWhere { Instruments.id.eq(instrId) }
        }
    }

    suspend fun replaceSourceInstruments(symbols: List<InstrId>) {
        if(symbols.isEmpty()){
            return
        }
        DbHelper.updateDatabase("insert instruments") {
            Instruments.deleteWhere { Instruments.sourceName eq symbols.first().source }

            Instruments.batchReplace(symbols) {
                this[Instruments.id] = it.id
                this[Instruments.code] = it.code
                this[Instruments.sourceName] = it.source
                this[Instruments.market] = it.market
                this[Instruments.payload] = ExposedBlob(JsonHelper.toJsonBytes(it))
            }
        }
    }
}