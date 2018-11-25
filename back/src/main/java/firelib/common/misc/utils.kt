package firelib.common.misc

import firelib.common.Trade
import java.text.DecimalFormat


val filterThresholdInVariance: Int = 10

fun dbl2Str(vv: Double, decPlaces: Int): String {
    var dp = if (decPlaces > 0) "#." else "#"
    for (a in -1..decPlaces) {
        dp += "#"
    }
    val df = DecimalFormat(dp)
    return df.format(vv)
}


private fun toTradingCasesInt(trades: List<Trade>): List<Pair<Trade, Trade>> {
    val generator = StreamTradeCaseGenerator()
    return trades.flatMap(generator)
}

fun pnlForCase(cas: Pair<Trade, Trade>): Double {
    return cas.first.moneyFlow() + cas.second.moneyFlow()
}

private fun filterTradingCases(tradingCases: List<Pair<Trade, Trade>>): List<Pair<Trade, Trade>> {
    return tradingCases
}

fun toTradingCases(trades: List<Trade>): List<Pair<Trade, Trade>> {
    var ret = ArrayList<Pair<Trade, Trade>>()

    trades.groupBy(Trade::security).values.forEach { trds->
        run {
            ret.addAll(toTradingCasesInt(trds))
        }
    }
    return ret
}

fun <T> instanceOfClass(className: String): T {
    return Class.forName(className).newInstance() as T
}
