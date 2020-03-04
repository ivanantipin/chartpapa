package com.firelib.transaq

import firelib.core.domain.InstrId
import firelib.common.Order
import firelib.core.domain.OrderType
import firelib.core.domain.Side

object TrqCommandHelper {

    fun getPortfolio(client: String): String {
        return "<command id=\"get_portfolio\" client=\"${client.toLowerCase()}\"/>"
    }


    fun connectCmd(login: String, passwd: String, host: String, port: String): String {
        return "<command id=\"connect\">" +
                "<login>" + login + "</login>" +
                "<password>" + passwd + "</password>" +
                "<host>" + host + "</host>" +
                "<port>" + port + "</port>" +
                "<rqdelay>100</rqdelay>" +
                "<session_timeout>100</session_timeout> " +
                "<request_timeout>20</request_timeout>" +
                "</command>"
    }

    fun disconnectCmd(): String {
        return "<command id=\"disconnect\"/>"
    }

    fun statusCmd(): String {
        return "<command id=\"server_status\"/>"
    }

    fun securitiesCmd(): String {
        return "<command id=\"get_securities\"/>"
    }

    fun subscribeCmd(instr: Array<InstrId>): String {
        return """
            <command id="subscribe">
                <alltrades>
                    ${instr.map { mapInstr(it) }.joinToString()}
                </alltrades>
            </command>            
        """.trimIndent()
    }

    fun unsubscibeCmd(instr: Array<InstrId>): String {
        return ""
    }


    /*
    Security(secid=26, active=true,
    board=TQBR, market=1,
    sec_tz=Russian Standard Time,
    seccode=SBER, shortname=Сбербанк, decimals=2, minstep=0.01, lotsize=10, point_cost=1, bymarket=null, sectype=SHARE, MIC=null, ticker=null, currency=null, instrclass=E),
     */

    fun getHistory(seccode: String, board: String, kind: String, count: Int): String {
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
        return """
            <command id="subscribe">
                <alltrades>
                        <security>
                            <board>${board}</board>
                            <seccode>${seccode}</seccode>
                        </security>
                
                </alltrades>
            </command>            
        """.trimIndent()
    }


    private fun mapInstr(it: InstrId): String {
        return """
            <security>
                <board>${it.board}</board>
                <seccode>${it.code}</seccode>
            </security>
            """.trimIndent()
    }

    fun newOrder(order: Order, client: String): String {
        return """
            <command id="neworder">
                ${mapInstr(order.instr)}
                <client>${client}</client>
                <price>${order.price}</price>
                <quantity>${order.qtyLots}</quantity>
                <buysell>${if (order.side == Side.Buy) "B" else "S"}</buysell>
                ${if (order.orderType == OrderType.Market) "<bymarket/>" else ""}
                <brokerref>примечание</brokerref>
                <unfilled>PutInQueue</unfilled>
            </command>    
        """.trimIndent()
    }

    fun cancelOrder(transactionid: String): String {
        return "<command id=\"cancelorder\"><transactionid>${transactionid}</transactionid></command>"
    }

    fun changePassword(old : String, new : String) : String{
        return "<command id=\"change_pass\" oldpass=\"${old}\" newpass=\"${new}\"/>"
    }


}