package firelib.core.misc

import firelib.common.Trade
import java.util.*
import kotlin.collections.ArrayList


class StreamTradeCaseGenerator {

    val posTrades = LinkedList<Trade>()

    fun getPosition() : List<Trade>{
        return posTrades.toList()
    }

    fun genClosedCases(trade : Trade) : List<Pair<Trade,Trade>> {

        val tradingCases = ArrayList<Pair<Trade, Trade>>(2)

        makeCaseWithPositionTrades(posTrades, tradingCases, trade)

        tradingCases.forEach { require(it.first.side() != it.second.side(),{"trading must have different sides"}) }

        tradingCases.sortBy {it.first.dtGmt.toEpochMilli()}

        return tradingCases

    }

    fun makeCaseWithPositionTrades(posTrades: LinkedList<Trade>, tradingCases: ArrayList<Pair<Trade, Trade>>, trade: Trade) {
        if(posTrades.isEmpty() || posTrades.peek().side() == trade.side()){
            posTrades.add(trade)
            return
        }
        var residualAmt = trade.qty
        val lastPositionTrade = posTrades.poll()
        if (lastPositionTrade.qty >= residualAmt) {
            val tradeSplit = lastPositionTrade.split(residualAmt)
            tradingCases += Pair(tradeSplit.first, trade.split(residualAmt).first)
            if (tradeSplit.second.qty != 0) posTrades.add(tradeSplit.second)
        }else{
            residualAmt -= lastPositionTrade.qty
            val tradeSplit = trade.split(lastPositionTrade.qty)
            tradingCases += Pair(lastPositionTrade, tradeSplit.first)
            makeCaseWithPositionTrades(posTrades, tradingCases,tradeSplit.second)
        }
    }
}