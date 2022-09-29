package firelib.iqfeed

import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.time.LocalDateTime

object IntervalTransformer {

    fun transform(interval: Interval, ohlcs: List<Ohlc>): List<Ohlc> {
        val ser = ContinousOhlcSeries(interval)
        ser.add(ohlcs)
        return ser.getDataSafe()
    }
}