package firelib.common.misc

import firelib.common.Trade
import java.text.DecimalFormat


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

fun toTradingCases(trades: List<Trade>): List<Pair<Trade, Trade>> {
    return trades.groupBy(Trade::security).values.flatMap { toTradingCasesInt(it) }
}