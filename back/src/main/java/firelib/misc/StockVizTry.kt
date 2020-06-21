package firelib.misc

import firelib.common.Trade
import firelib.core.domain.Side
import firelib.core.misc.pnl
import org.openapitools.client.apis.InstrumentsApi
import org.openapitools.client.apis.PortfoliosApi
import org.openapitools.client.models.NewInstrument
import org.openapitools.client.models.NewTrade
import org.openapitools.client.models.Portfolio
import java.time.ZoneOffset


object StockVizTradeWriter{
    fun writePairs(list : List<Pair<Trade,Trade>>, portfolio : String){


        val api = PortfoliosApi()

        val instrumentsApi = InstrumentsApi()

        val instrs = instrumentsApi.instrumentsList(1, 10000).results.groupBy { it.identifier }


        val distinct = list.map {
            val instr = it.first.order.instr
            NewInstrument("${instr.code.toUpperCase()}.MICEX", instr.code.toUpperCase(), "MICEX", null, null)
        }.distinct().filter {
            !instrs.containsKey(it.identifier)
        }
        if(distinct.isNotEmpty()){
            println("written ${distinct}")
            instrumentsApi.instrumentsAddCreate(distinct.toTypedArray())

        }

        api.portfoliosAddTradesCreate(portfolio, list.map {
            val t0 = it.first
            val t1 = it.second
            NewTrade(
                tradeId = t0.tradeNo,
                side = if(t0.side() == Side.Sell) NewTrade.Side.short else NewTrade.Side.long,
                qty = t0.qty.toBigDecimal(),
                openPrice = t0.price.toBigDecimal(),
                openTime = t0.dtGmt.atOffset(ZoneOffset.UTC) ,
                symbol =  "${t0.security().toUpperCase()}.MICEX",
                pnl = it.pnl().toBigDecimal(),
                portfolio = portfolio,
                closePrice = t1.price.toBigDecimal(),
                closeTime = t1.dtGmt.atOffset(ZoneOffset.UTC) ,
                continuousTags = emptyMap<String,String>(),
                discreteTags = emptyMap<String,String>()
            )
        }.toTypedArray())
    }
}

fun main() {
    val api = PortfoliosApi()
    api.portfoliosClearCreate("RealDivModel")
}
