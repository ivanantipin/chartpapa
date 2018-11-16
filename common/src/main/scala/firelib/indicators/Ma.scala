package firelib.indicators
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
class Ma(val period: Int,
          ts: TimeSeries[Ohlc], val calcSko : Boolean = false)
  extends Indicator[Double] with (TimeSeries[Ohlc] => Unit) {

    ts.onNewBar.subscribe(this)

    val maa = new SimpleMovingAverage(period, calcSko)

    override def value: Double = maa.value

    def sko: Double = maa.sko

    override def apply(ts: TimeSeries[Ohlc]): Unit = {
        if (!ts(0).interpolated) {
            maa.add(ts(0).C)
        }
    }
}
