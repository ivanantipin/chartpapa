package com.example

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import java.util.*

//marker interface
interface TrqMsg

data class Kind(
        @set:JsonProperty("id")
        var id: String?,
        @set:JsonProperty("period")
        var period: String?,
        @set:JsonProperty("name")
        var name: String?)

data class CandleKinds(
        @set:JsonProperty("kind")
        var kinds: List<Kind>) : TrqMsg


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


/*
</decimals><minstep>Шаг цены</minstep><lotsize>Размер лота</lotsize><point_cost>Стоимость пункта цены
</point_cost><opmask usecredit="yes/no" bymarket="yes/no" nosplit="yes/no" fok="yes/no" ioc="yes/no "/><sectype>Тип
бумаги
</sectype><sec_tz>имя таймзоны инструмента (типа "Russian Standard Time", "USA=Eastern Standard Time"),содержит секцию
CDATA
</sec_tz><quotestype>0 -без стакана; 1 -стакан типа OrderBook; 2 -стакан типа Level2</quotestype><MIC>код биржи листинга
по стандарту ISO
</MIC><ticker>тикер на бирже листинга</ticker><currency>валютацены</currency>
 */


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


data class Securities (
        @set:JsonProperty(value = "security")
        var securities: List<Security>
) : TrqMsg


data class Market(var id: String?,
                  @JsonRawValue
                  var content: String?)

data class Markets(
        @set:JsonProperty(value = "market")
        var markets: List<Market>) : TrqMsg

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


data class FinamTrade(
        var price: String?
)


data class AllTrades(
        @set:JsonProperty("trade")
        var trades: List<FinamTrade> = ArrayList()
) : TrqMsg

/*
<orders>
    <order transactionid="идентификатор транзакции сервера Transaq">
    <orderno>биржевой номер заявки</orderno>
    <secid>идентификатор бумаги</secid>
    <board>идентификатор борда</board>
    <seccode>код инструмента</seccode>
    <client>идентификатор клиента</client>
    <union>Код юниона</union>
    <status>статус заявки (см. ниже в таблице 5)</status>
    <buysell>покупка (B) / продажа (S)</buysell>
    <time>время регистрации заявки биржей</time>
    <expdate>дата экспирации (только для ФОРТС)</expdate>(задается в формате 23.07.2012 00:00:00 (не обязательно)
    <origin_orderno>биржевой номер родительской заявки</origin_orderno>
    <accepttime>время регистрации заявки сервером Transaq(только для условных заявок)</accepttime>
    <brokerref>примечание</brokerref>
    <value>объем заявки в валюте инструмента</value>
    <accruedint>НКД</accruedint>
    <settlecode>Код поставки (значение биржи, определяющее правила расчетов -смотрите подробнее в документах биржи)
    </settlecode>
    <balance>Неудовлетворенный остаток объема заявки в лотах (контрактах)</balance>
    <price>Цена</price>
    <quantity>Количество</quantity>
    <hidden>Скрытое количество в лотах</hidden>
    <yield>Доходность</yield>
    <withdrawtime>Время снятия заявки, 0 для активных</withdrawtime>
    <condition>Условие, см. newcondorder</condition>
    <conditionvalue>Цена для условной заявки, либо обеспеченность в процентах</conditionvalue>
    <validafter>с какого момента времени действительна (см. newcondorder)</validafter>
    <validbefore>до какого момента действительно (см. newcondorder)</validbefore>
    <maxcomission>максимальная комиссия по сделкам заявки</maxcomission>
    <result>сообщение биржи в случае отказа выставить заявку</result>
</order>
</orders>
 */

data class TrqOrder(var transactionid: String,
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
        var securities : List<PortfolioSecurity>
) : TrqMsg

data class TrqClient(
        var id: String?,
        var type: String?,
        var currency: String?,
        var market: String?,
        var union: String?,
        var forts_acc: String?
)


