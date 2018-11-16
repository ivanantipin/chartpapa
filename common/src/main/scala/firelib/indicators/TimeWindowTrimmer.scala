package firelib.indicators


import java.time.Duration
import java.util

import scala.collection.mutable.ArrayBuffer


class TimeWindowTrimmer(val dur : Duration) extends (OrderInfo=>Seq[OrderInfo])  {

    private val queue = new util.LinkedList[OrderInfo]()

    override def apply(oi: OrderInfo): Seq[OrderInfo] = {
        queue.add(oi)
        val head = queue.getLast.dt
        val ret = new ArrayBuffer[OrderInfo](0)
        while(head.getEpochSecond - queue.getFirst.dt.getEpochSecond > dur.getSeconds){
            ret += queue.poll()
        }
        ret
    }
}
