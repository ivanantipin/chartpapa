package firelib.indicators

import java.time.Instant
import java.util.*

class Donchian(val windowSec: Int, val useMax: Boolean) {

    data class Node(val value: Double, val time: Instant)

    class NodeComparator : Comparator<Node> {
        override fun compare(o1: Node, o2: Node): Int {
            val cmp = o1.value.compareTo(o2.value)
            if (cmp != 0) {
                return cmp;
            }
            return o1.time.compareTo(o2.time)
        }
    }

    private val queue = LinkedList<Node>()

    private val tree = TreeSet<Node>(NodeComparator())

    private var cnt = 0

    fun nextCount(): Int {
        cnt += 1; return cnt
    }

    fun max(): Double = tree.last().value

    fun min(): Double = tree.first().value

    fun value() = if (useMax) max() else min()

    fun addMetric(t: Instant, m: Double) {
        val node: Node = Node(m, t)
        queue.add(node)
        tree.add(node)
        val head = queue.last.time
        while (head.epochSecond - queue.first.time.epochSecond > windowSec) {
            val nn = queue.poll()
            tree.remove(nn)
        }
    }
}