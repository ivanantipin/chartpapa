package firelib.common.timeseries

import firelib.common.misc.SubChannel
import firelib.domain.Ohlc

trait TimeSeries[T] {

    def count : Int

    def apply(idx: Int): T

    val onNewBar : SubChannel[TimeSeries[T]]

}

trait OhlcSeriesUtils{
    this : TimeSeries[Ohlc] =>

    def diff(len : Int) : Double = apply(0).C - apply(len - 1).C

}

trait OhlcSeries extends TimeSeries[Ohlc] with OhlcSeriesUtils{}



