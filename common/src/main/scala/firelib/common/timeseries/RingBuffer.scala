package firelib.common.timeseries

import scala.reflect.ClassTag

class RingBuffer[T:ClassTag](val length: Int, func: () => T) {

    var data = new Array[T](0)
    var count = 0
    var head = 0
    adjustSizeIfNeeded(length)

    def apply(idx: Int): T = data(calcIdx(idx))

    private def calcIdx(idx: Int): Int = {
        assert(idx >= 0 && idx < data.length)
        (head - idx + length) % length
    }

    def update(idx : Int, value : T ) = data(calcIdx(idx)) = value

    def add(t : T) : Unit = {
        head = (head + 1) % length
        count += 1
        update(0,t)
    }

    def adjustSizeIfNeeded(historySize: Int): Unit = {
        if (historySize > data.length) {
            data = Array.fill[T](length) {func()}
        }
    }
}
