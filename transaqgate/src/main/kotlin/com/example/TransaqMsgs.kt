package com.example

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRawValue
import com.fasterxml.jackson.annotation.JsonRootName
import java.util.ArrayList


data class Kind(
        @set:JsonProperty("id")
        var id : String?,
        @set:JsonProperty("period")
        var period : String?,
        @set:JsonProperty("name")
        var name : String?)

data class CandleKinds(
        @set:JsonProperty("kind")
        var kinds : List<Kind>)


data class Board(
        var id : String?,
        var name : String?,
        var market : String?,
        var type : String?
)

data class Boards(
        @set:JsonProperty(value = "board")
        var boards : List<Board>
)


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
        var secid : String?,
        var active : Boolean?,
        var board : String?,
        var market : String?,
        var sec_tz : String?,
        var seccode : String?,
        var shortname : String?,
        var decimals : String?,
        var minstep : String?,
        var lotsize : String?,
        var point_cost : String?,
        var bymarket : String?,
        var sectype : String?,
        var MIC : String?,
        var ticker : String?,
        var currency : String?,
        var instrclass : String?

)




data class Securities(
        @set:JsonProperty(value = "security")
        var securities: List<Security>
)


data class Market(var id : String?,
                  @JsonRawValue
                  var content : String?)

data class Markets(
        @set:JsonProperty(value = "market")
        var markets : List<Market>){

}

data class SecInfoUpd(
        var secid : String?,
        var seccode : String?,
        var market : String?,
        var buy_deposit : String?,
        var sell_deposit : String?,
        var minprice : String?,
        var maxprice : String?

)


data class FinamTrade(
        @set:JsonProperty("price")
        var price: String?
)


data class AllTrades(
        @set:JsonProperty("trade")
        var trades: List<FinamTrade> = ArrayList()
)
