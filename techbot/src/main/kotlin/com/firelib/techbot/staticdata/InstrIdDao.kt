package com.firelib.techbot.staticdata

import com.firelib.techbot.persistence.Subscriptions.default
import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table

/*
data class InstrId(val id: String = "N/A",
                   val name: String = "N/A",
                   val market: String = "NA",
                   val code: String = "N/A",
                   val source: String = "N/A",
                   val minPriceIncr: BigDecimal = BigDecimal.ONE.divide(10.toBigDecimal()),
                   val lot: Int = 1,
                   val board : String = "N/A"

 */

object Instruments : Table() {
    val id = varchar("id", 10)
    val code = varchar("code", 10)
    val market = varchar("market", 10)
    val payload = blob("payload")

    init {
        index(true, id, code, market)
    }
}


class InstrIdDao {
    fun loadAll(): List<InstrId>{
        TODO()
    }
    fun add(instrId: List<InstrId>){
        TODO()
    }
}