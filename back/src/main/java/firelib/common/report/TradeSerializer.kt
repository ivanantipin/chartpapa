package firelib.common.report

import firelib.common.Side
import firelib.common.Trade
import firelib.common.misc.dbl2Str
import firelib.common.misc.pnlForCase
import firelib.common.misc.toStandardString
import firelib.common.report.ReportConsts.Companion.decPlaces

class TradeSerializer : ReportConsts {

    val colsDef = arrayOf(
            makeMetric("Ticker") {it.first.security()},
            makeMetric("OrderId0") {it.first.order.id},
            makeMetric("OrderId1") {it.second.order.id},
            makeMetric("BuySell") {if(it.first.side() == Side.Buy) "1" else "-1"},
        makeMetric("EntryDate") {it.first.dtGmt.toStandardString()},
        makeMetric("EntryPrice") {dbl2Str(it.first.price, decPlaces)},
        makeMetric("ExitDate") {it.second.dtGmt.toStandardString()},
        makeMetric("ExitPrice") {dbl2Str(it.second.price, decPlaces)},
        makeMetric("Pnl") {dbl2Str(pnlForCase(it), decPlaces)},
        makeMetric("Qty") { it : Pair<Trade,Trade>->it.first.qty.toString()},
        makeMetric("MAE") { it : Pair<Trade,Trade>->dbl2Str(it.second.tradeStat.MAE(), decPlaces)},
        makeMetric("MFE") { it : Pair<Trade,Trade>->dbl2Str(it.second.tradeStat.MFE(), decPlaces)}
    )

    fun makeMetric(name : String, funct: (Pair<Trade,Trade>)->String): Pair<String, (Pair<Trade, Trade>) -> String> {
        return Pair(name,funct)
    }

    fun getHeader(): List<String> = (colsDef.map {it.first})

    fun serialize(t : Pair<Trade,Trade>) : List<String> {
        return colsDef.map({it.second}).map({it(t)})
    }
}