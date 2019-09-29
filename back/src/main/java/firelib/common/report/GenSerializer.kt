package firelib.common.report

import firelib.common.Order
import firelib.common.Trade
import firelib.common.misc.pnl
import firelib.common.report.SqlTypeMapper.mapType
import firelib.domain.Ohlc
import firelib.domain.Side
import java.time.Instant
import kotlin.reflect.KType
import kotlin.reflect.jvm.reflect


object SqlTypeMapper{

    fun mapType(retType: KType): String {
        if (retType.classifier == String::class) {
            return "VARCHAR"
        }
        if (retType.classifier == Double::class) {
            return "DOUBLE PRECISION"
        }
        if (retType.classifier == Int::class) {
            return "INT"
        }
        if (retType.classifier == Long::class) {
            return "INT"
        }

        if (retType.classifier == Instant::class) {
            return "TIMESTAMPTZ"
        }

        throw RuntimeException("not supported type $retType")
    }


}

class ColDef<I,T>(val name : String, val extract : (I)->T, val typeOverride : KType? = null){
    fun getSqlType(): String{
        return mapType(typeOverride ?: extract.reflect()!!.returnType)
    }

}

fun <I,T> makeMetric(name : String, funct: (I)->T): ColDef<I,T> {
    return ColDef<I,T>(name,funct)
}


val tradeCaseColDefs : Array<ColDef<Pair<Trade,Trade>,out Any>> = arrayOf(
        makeMetric("Ticker", {it.first.security()}),
        makeMetric("OrderId0", {it.first.order.id}),
        makeMetric("OrderId1") {it.second.order.id},
        makeMetric("EntryPriceTime") {it.first.priceTime},
        makeMetric("ExitPriceTime") {it.second.priceTime},
        makeMetric("BuySell") {if(it.first.side() == Side.Buy) 1 else -1},

        makeMetric("EntryDate") {it.first.dtGmt},
        makeMetric("EntryPrice") {it.first.price},
        makeMetric("ExitDate") {it.second.dtGmt},
        makeMetric("ExitPrice") {it.second.price},
        makeMetric("Pnl") {it.pnl()},
        makeMetric("Qty") { it.first.qty},
        makeMetric("MAE") { it.second.tradeStat.MAE()},
        makeMetric("MFE") { it.second.tradeStat.MFE()}
)

val orderColsDefs : Array<ColDef<Order,out Any>> = arrayOf(
        makeMetric("Ticker", { it.security }),
        makeMetric("OrderId", { it.id }),
        makeMetric("OrderType", { it.orderType.name }),
        makeMetric("BuySell", { if (it.side == Side.Buy) "1" else "-1" }),
        makeMetric("EntryDate", { it.placementTime}),
        makeMetric("Price", { it.price}),
        makeMetric("Qty", { it.qty})
)

val ohlcColsDef : Array<ColDef<Ohlc,out Any>> = arrayOf(
        makeMetric("DT", { it.dtGmtEnd}),
        makeMetric("O") { it.open },
        makeMetric("H") { it.high},
        makeMetric("L") { it.low },
        makeMetric("C") { it.close}
)




fun <I> getHeader(colsDef : Array<ColDef<I,out Any>>): Map<String,String> {
    return colsDef.associateBy ({it.name},{it.getSqlType()})
}

fun <I> toMapForSqlUpdate(t : I, colsDef : Array<ColDef<I,out Any>>) : Map<String,Any> {
    return colsDef.associateBy ({ it.name}, { it.extract(t) })
}

fun main(args : Array<String>){
    //println(GenSerializer().getHeader())

}