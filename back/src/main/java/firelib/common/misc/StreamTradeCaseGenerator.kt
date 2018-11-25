package firelib.common.misc

import firelib.common.Trade
import java.util.*
import kotlin.collections.ArrayList


class StreamTradeCaseGenerator : ((Trade)->List<Pair<Trade,Trade>>){

    val posTrades = Stack<Trade>()

    override fun invoke(trade : Trade) : List<Pair<Trade,Trade>> {

        val tradingCases = ArrayList<Pair<Trade, Trade>>(2)

        makeCaseWithPositionTrades(posTrades, tradingCases, trade)

        tradingCases.forEach({tc->assert(tc.first.side() != tc.second.side(),{"trading must have different sides"})})

        tradingCases.sortBy {it.first.dtGmt.toEpochMilli()}

        return tradingCases

    }

    fun makeCaseWithPositionTrades(posTrades: Stack<Trade>, tradingCases: ArrayList<Pair<Trade, Trade>>, trade: Trade) {
        if(posTrades.isEmpty() || posTrades.peek().side() == trade.side()){
            posTrades.push(trade)
            return
        }
        var residualAmt = trade.qty
        val lastPositionTrade = posTrades.pop()
        if (lastPositionTrade.qty >= residualAmt) {
            val tradeSplit = lastPositionTrade.split(residualAmt)
            tradingCases += Pair(tradeSplit.first, trade.split(residualAmt).first)
            if (tradeSplit.second.qty != 0) posTrades.push(tradeSplit.second)
        }else{
            residualAmt -= lastPositionTrade.qty
            val tradeSplit = trade.split(lastPositionTrade.qty)
            tradingCases += Pair(lastPositionTrade, tradeSplit.first)
            makeCaseWithPositionTrades(posTrades, tradingCases,tradeSplit.second)
        }
    }
}