package firelib.common.misc

import java.time.{Duration, Instant}

import firelib.domain.Timed

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.language.{implicitConversions, postfixOps}

class WindowSlicer[T <: Timed](val windowDuration : Duration) extends (T=>Seq[T]){

    private val queue = new mutable.Queue[T]()

    var writeBefore: Instant = Instant.MIN

    var lastTime = Instant.MIN

    def checkTail() : Seq[T] = {
        val ret = new ArrayBuffer[T](0)
        while (queue.nonEmpty && queue.head.time.isBefore(writeBefore)) {
            ret += queue.dequeue()
        }
        while (queue.nonEmpty && queue.last.time.getEpochSecond - queue.head.time.getEpochSecond > windowDuration.getSeconds) {
            queue.dequeue()
        }
        ret
    }

    def updateWriteBefore() {
        writeBefore = lastTime.plus(windowDuration)
    }

    override def apply(oh: T): Seq[T] = {
        lastTime = oh.time
        queue += oh
        checkTail()
    }
}
