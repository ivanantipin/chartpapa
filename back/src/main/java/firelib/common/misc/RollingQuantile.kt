package firelib.common.misc

import java.util.*


class  RollingQuantile(val quantile: Double, val length: Int) {

    data class Node(val value: Double, var isLeft: Boolean, val counter : Int)

    class NodeComparator : Comparator<Node>{
        override fun compare(o1: Node, o2: Node): Int {
            val cmp = o1.value.compareTo(o2.value)
            if (cmp != 0) {
                return cmp;
            }
            return o1.counter - o2.counter
        }
    }

    private val queue = LinkedList<Node>()

    private val left = TreeSet<Node>(NodeComparator())
    private val right = TreeSet<Node>(NodeComparator())

    private var cnt = 0

    fun count ()= cnt

    fun value(): Double {
        if (left.isEmpty() && right.isEmpty()) {
            return 0.0
        }
        if (left.isEmpty()) {
            return right.first().value
        }
        if (right.isEmpty()) {
            return left.last().value
        }
        return (left.last().value + right.first().value) / 2
    }

    fun addMetric(m: Double) {
        assert(!m.isNaN() && !m.isInfinite())
        if (queue.size >= length) {
            val node = queue.poll()
            if (node.isLeft) {
                assert(left.remove(node))
            }
            else {
                assert(right.remove(node))
            }
        }
        cnt += 1

        if (m > value()) {
            val n = Node(m, false, cnt)
            queue.add(n)
            right.add(n)
        }
        else {
            val n = Node(m, true, cnt)
            queue.add(n)
            left.add(n)
        }
        balance()
    }

    private fun balance() {
        val diff = left.size / quantile - right.size / (1 - quantile)
        if (diff > 0) {
            val amDiff = (left.size - 1) / quantile - (right.size + 1) / (1 - quantile)
            if (Math.abs(amDiff) < Math.abs(diff)) {
                val leftMax = left.last()
                assert(left.remove(leftMax))
                right.add(leftMax)
                leftMax.isLeft = false

            }
        }
        else {
            val amDiff = (left.size + 1) / quantile - (right.size - 1) / (1 - quantile)
            if (Math.abs(amDiff) < Math.abs(diff)) {
                val rightMin = right.first()
                assert(right.remove(rightMin))
                left.add(rightMin)
                rightMin.isLeft = true
            }
        }
    }
}