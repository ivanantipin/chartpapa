package firelib.common.misc

import com.funstat.store.MdStorageImpl
import firelib.common.Trade
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.model.DivHelper
import firelib.common.reader.MarketDataReaderDb
import firelib.common.reader.ReaderDivAdjusted
import java.text.DecimalFormat
import java.time.Instant


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
    return trades.flatMap({generator.addTrade(it)})
}

fun Pair<Trade, Trade>.pnl() : Double{
    return this.first.moneyFlow() + this.second.moneyFlow()
}

fun List<Trade>.toTradingCases() : List<Pair<Trade,Trade>>{
    return this.groupBy(Trade::security).values.flatMap { toTradingCasesInt(it) }
}

