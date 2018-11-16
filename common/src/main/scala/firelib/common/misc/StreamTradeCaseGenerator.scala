package firelib.common.misc

import firelib.common.Trade

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer



class StreamTradeCaseGenerator extends (Trade=>Seq[(Trade,Trade)]){

    val posTrades = new mutable.Stack[Trade]()

    def apply(trade : Trade) : Seq[(Trade,Trade)] = {

        val tradingCases = new ArrayBuffer[(Trade, Trade)](2)

        makeCaseWithPositionTrades(posTrades, tradingCases, trade)

        tradingCases.foreach(tc=>assert(tc._1.side != tc._2.side,"trading case must have different sides"))

        tradingCases.sortBy(_._1.dtGmt.toEpochMilli)

        return tradingCases

    }

    def makeCaseWithPositionTrades(posTrades: mutable.Stack[Trade], tradingCases: ArrayBuffer[(Trade, Trade)], trade: Trade) {
        if(posTrades.isEmpty || posTrades.top.side == trade.side){
            posTrades.push(trade)
            return
        }
        var residualAmt = trade.qty
        val lastPositionTrade = posTrades.pop()
        if (lastPositionTrade.qty >= residualAmt) {
            val tradeSplit = lastPositionTrade.split(residualAmt)
            tradingCases += ((tradeSplit._1, trade.split(residualAmt)._1))
            if (tradeSplit._2.qty != 0) posTrades.push(tradeSplit._2)
        }else{
            residualAmt -= lastPositionTrade.qty
            val tradeSplit: (Trade, Trade) = trade.split(lastPositionTrade.qty)
            tradingCases += ((lastPositionTrade, tradeSplit._1))
            makeCaseWithPositionTrades(posTrades, tradingCases,tradeSplit._2)
        }
    }


}
