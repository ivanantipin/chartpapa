package firelib.common.timeseries

import firelib.common.misc.NonDurableChannel

import scala.reflect.ClassTag

class TimeSeriesImpl[T:ClassTag] (val length: Int, func: () => T) extends TimeSeries[T] {

    val data = new RingBuffer[T](length, func)

    def adjustSizeIfNeeded(historySize: Int) = data.adjustSizeIfNeeded(historySize)

    def count = data.count

    def apply(idx: Int): T = data(idx)

    def add(t : T) = {
        onNewBar.publish(this)
        data.add(t)
    }

    override val onNewBar = new NonDurableChannel[TimeSeries[T]]

}
