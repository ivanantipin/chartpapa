package com.firelib.transaq

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import java.util.*

//marker interface
interface TrqMsg

data class Kind(
    var id: String,
    var period: Int,
    var name: String?
)

data class CandleKinds(
    @set:JsonProperty("kind")
    var kinds: List<Kind>
) : TrqMsg


data class Board(
    var id: String?,
    var name: String?,
    var market: String?,
    var type: String?
)

data class Boards(
    @set:JsonProperty(value = "board")
    var boards: List<Board>
) : TrqMsg

data class Candle(
    var date: String?,
    var open: Double?,
    var high: Double?,
    var low: Double?,
    var close: Double?,
    var volume: Int?,
    var oi: Int?
)

data class Candles(
    var secid: String?,
    var status: String?,
    var board: String?,
    var seccode: String?,
    @set:JsonProperty(value = "candle")
    var candles: List<Candle>
) : TrqMsg

data class Security(
    var secid: String?,
    var active: Boolean?,
    var board: String?,
    var market: String?,
    var sec_tz: String?,
    var seccode: String?,
    var shortname: String?,
    var decimals: String?,
    var minstep: String?,
    var lotsize: String?,
    var point_cost: String?,
    var bymarket: String?,
    var sectype: String?,
    var MIC: String?,
    var ticker: String?,
    var currency: String?,
    var instrclass: String?

)


data class Securities(
    @set:JsonProperty(value = "security")
    var securities: List<Security>
) : TrqMsg

/*
server_status server_tz="Russian Standard Time" id="0" connected="true"/>
 */
data class ServerStatus(val server_tz: String?, var id: String?, var connected: String?) : TrqMsg

data class Market(
    var id: String?,
    var text: String?
)

data class Markets(
    @set:JsonProperty(value = "market")
    var markets: List<Market>
) : TrqMsg

data class TrqResponse(
    var success: Boolean,
    var transactionid: String?,
    var message: String?

) : TrqMsg

data class SecInfoUpd(
    var secid: String?,
    var seccode: String?,
    var market: String?,
    var buy_deposit: String?,
    var sell_deposit: String?,
    var minprice: String?,
    var maxprice: String?

) : TrqMsg


data class TrqTrade(
    var secid: String?,
    var tradeno: String?,
    var orderno: String?,
    var board: String?,
    var seccode: String?,
    var client: String?,
    var buysell: String?,
    var union: String?,
    var time: String?,
    var brokerref: String?,
    var value: String?,
    var comission: String?,
    var price: String?,
    var quantity: String?,
    var items: String?,
    var yield: String?,
    var currentpos: String?,
    var accruedint: String?,
    var tradetype: String?,
    var settlecode: String?
)

data class Tick(
    var tradeno: String?,
    var board: String?,
    var time: String?,
    var price: Double,
    var quantity: Int,
    var buysell: String?,
    var seccode: String?,
    var period: String?
)

data class AllTrades(
    @set:JsonProperty("trade")
    var ticks: List<Tick>
) : TrqMsg


data class TrqTrades(
    @set:JsonProperty("trade")
    var trades: List<TrqTrade> = ArrayList()
) : TrqMsg

data class TrqOrder(
    var transactionid: String,
    var orderno: String?,
    var secid: String?,
    var board: String?,
    var seccode: String?,
    var client: String?,
    var union: String?,
    var status: String?,
    var buysell: String?,
    var time: String?,
    var expdate: String?,
    var origin_orderno: String?,
    var accepttime: String?,
    var brokerref: String?,
    var value: String?,
    var accruedint: String?,
    var settlecode: String?,
    var balance: String?,
    var price: String?,
    var quantity: String?,
    var hidden: String?,
    var yield: String?,
    var withdrawtime: String?,
    var condition: String?,
    var conditionvalue: String?,
    var validafter: String?,
    var validbefore: String?,
    var maxcomission: String?,
    var result: String?
) : TrqMsg


data class ValuePart(
    var register: String?,
    var open_balance: String?,
    var bought: String?,
    var sold: String?,
    var settled: String?,
    var balance: String?
)

data class Money(
    var open_balance: String?,
    var bought: String?,
    var sold: String?,
    var settled: String?,
    var balance: String?,
    var tax: String?
)

data class PortfolioSecurity(
    var secid: String?,
    var market: String?,
    var seccode: String?,
    var price: String?,
    var open_balance: String?,
    var bought: String?,
    var sold: String?,
    var balance: String?,
    var balance_prc: String?,
    var unrealized_pnl: String?,
    var buying: String?,
    var selling: String?,
    var cover: String?,
    var init_margin: String?,
    var riskrate_long: String?,
    var riskrate_short: String?,
    var pnl_income: String?,
    var pnl_intraday: String?,
    var maxbuy: String?,
    var maxsell: String?
)

data class TrqPortfolio(
    var client: String?,
    var coverage_fact: String?,
    var coverage_plan: String?,
    var coverage_crit: String?,
    var open_equity: String?,
    var equity: String?,
    var cover: String?,
    var init_margin: String?,
    var pnl_income: String?,
    var pnl_intraday: String?,
    var leverage: String?,
    var margin_level: String?,
    @set:JsonProperty("security")
    var securities: List<PortfolioSecurity>
) : TrqMsg


data class TrqOrders(
    @set:JsonProperty("order")
    var orders: List<TrqOrder>
) : TrqMsg


data class TrqClient(
    var id: String?,
    var remove: Boolean?,
    var market: String?,
    var currency: String?,
    var type: String?,
    var union: String?,
    var forts_acc: String?
) : TrqMsg

data class MoneyPosition(
    var register: String?,
    var asset: String?,
    var client: String?,
    var union: String?,
    var shortname: String?,
    var saldoin: String?,
    var bought: String?,
    var sold: String?,
    var saldo: String?,
    var ordbuy: String?,
    var ordbuycond: String?,
    var comission: String?
)

data class SecPosition(
    var secid: String?,
    var market: String?,
    var seccode: String?,
    var register: String?,
    var client: String?,
    var union: String?,
    var shortname: String?,
    var saldoin: String?,
    var saldomin: String?,
    var bought: String?,
    var sold: String?,
    var saldo: String?,
    var ordbuy: String?,
    var ordsell: String?,
    var amount: String?,
    var equity: String?
)

data class Positions(
    var sec_position: SecPosition?,
    var money_position: MoneyPosition?
) : TrqMsg

data class Message(
    var date: String?,
    var urgent: String?,
    var from: String?,
    var text: String?
)

data class Messages(
    @set:JsonProperty("message")
    var orders: List<Message>
) : TrqMsg

fun main() {
    println(TrqParser.kotlinXmlMapper.readValue<Market>("<market>some</market>", Market::class.java))

}