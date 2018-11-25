package firelib.common.misc

import firelib.domain.Timed
import java.time.Duration
import java.time.Instant
import java.util.*

class WindowSlicer<T : Timed>(val windowDuration : Duration) : ((T)->List<T>){

    private val queue = LinkedList<T>()

    var writeBefore: Instant = Instant.MIN

    var lastTime = Instant.MIN

    fun checkTail() : List<T> {
        val ret = ArrayList<T>(0)
        while (queue.isNotEmpty() && queue.first.time().isBefore(writeBefore)) {
            ret += queue.removeFirst()
        }
        while (queue.isNotEmpty() && queue.last.time().epochSecond - queue.first.time().epochSecond > windowDuration.seconds) {
            queue.removeFirst()
        }
        return ret
    }

    fun updateWriteBefore() {
        writeBefore = lastTime.plus(windowDuration)
    }

    override fun invoke(oh: T): List<T> {
        lastTime = oh.time()
        queue += oh
        return checkTail()
    }
}