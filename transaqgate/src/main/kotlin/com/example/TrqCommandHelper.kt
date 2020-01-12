package com.example

import com.funstat.domain.InstrId
import firelib.common.Order
import firelib.domain.OrderType
import firelib.domain.Side

object TrqCommandHelper {
    fun getLoginCommand(login: String, passwd: String, host: String, port: String): String {
        return "<command id=\"connect\">" +
                "<login>" + login + "</login>" +
                "<password>" + passwd + "</password>" +
                "<host>" + host + "</host>" +
                "<port>" + port + "</port>" +
                "<rqdelay>100</rqdelay>" +
                "<session_timeout>1000</session_timeout> " +
                "<request_timeout>1000</request_timeout>" +
                "</command>"
    }

    fun getDisconnectCommand(): String {
        return "<command id=\"disconnect\"/>"
    }

    /*
    Security(secid=26, active=true,
    board=TQBR, market=1,
    sec_tz=Russian Standard Time,
    seccode=SBER, shortname=Сбербанк, decimals=2, minstep=0.01, lotsize=10, point_cost=1, bymarket=null, sectype=SHARE, MIC=null, ticker=null, currency=null, instrclass=E),
     */

    fun getHistory(seccode: String, board: String, kind : String, count : Int) : String{
return """
<command id="gethistorydata">
    <security>
        <board>${board}</board>
        <seccode>${seccode}</seccode>
    </security>
    <period>${kind}</period>
    <count>${count}</count>
    <reset>true</reset>
</command>    
""".trimIndent()
    }

    fun subscribe(seccode: String, board: String): String {
        val ret = """
<command id="subscribe">
    <alltrades>
            <security>
                <board>${board}</board>
                <seccode>${seccode}</seccode>
            </security>
    
    </alltrades>
</command>            
        """.trimIndent()

        return ret
    }

    fun getSubscribe(instr: Array<InstrId>): String {
        val ret = """
<command id="subscribe">
    <alltrades>
    ${instr.map { mapInstr(it) }.joinToString()}
    </alltrades>
</command>            
        """.trimIndent()

        println("ret ${ret}")

        return ret
    }


    private fun mapInstr(it: InstrId): String {
        return """
            <security>
                <board>${it.market}</board>
                <seccode>${it.code}</seccode>
            </security>
    """.trimIndent()
    }

    fun getBoard(instr: InstrId): String {
        return instr.market
    }

    fun newOrder(order: Order, client: String): String {
        return """
<command id="${order.id}">
    ${mapInstr(order.instr)}
    <client>${client}</client>
    <union>union code</union>
    <price>${order.price}</price>
    <quantity>${order.qtyLots}</quantity>
    <buysell>${if (order.side == Side.Buy) "B" else "S"}</buysell>
    ${if (order.orderType == OrderType.Market) "<bymarket/>" else ""}
    <brokerref>примечание</brokerref>(будет возвращено в составе структур orderи trade)
    <unfilled>PutInQueue</unfilled>(другиевозможные значения: FOK, IOC)
</command>    
""".trimIndent()
    }


    fun getSecurities(): String {
        return "<command id=\"get_securities\"/>"
    }

}