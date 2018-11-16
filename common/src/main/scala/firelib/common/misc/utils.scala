package firelib.common.misc

import java.text.DecimalFormat

import firelib.common.Trade

import scala.collection.mutable.ArrayBuffer


object utils {

    val filterThresholdInVariance: Int = 10

    def dbl2Str(vv: Double, decPlaces: Int): String = {
        var dp = if (decPlaces > 0) "#." else "#"
        for (a <- 1 to decPlaces) {
            dp += "#"
        }
        val df = new DecimalFormat(dp)
        return df.format(vv)
    }


    private def toTradingCasesInt(trades: Seq[Trade]): Seq[(Trade, Trade)] = {
        val generator = new StreamTradeCaseGenerator()
        return trades.flatMap(generator)
    }

    def pnlForCase(cas: (Trade, Trade)): Double = cas._1.moneyFlow + cas._2.moneyFlow

    private def filterTradingCases(tradingCases: Seq[(Trade, Trade)]): Seq[(Trade, Trade)] = {
        return tradingCases
    }

    def toTradingCases(trades: Seq[Trade]): Seq[(Trade, Trade)] = {
        var ret = new ArrayBuffer[(Trade, Trade)]()

        val groupedBySecurity: Map[String, Seq[Trade]] = trades.groupBy(t => t.security)

        groupedBySecurity.foreach(group => {
            ret ++= filterTradingCases(toTradingCasesInt(group._2))
        })
        return ret
    }

    def instanceOfClass[T](className : String) : T = {
        return Class.forName(className).newInstance().asInstanceOf[T]
    }

}
