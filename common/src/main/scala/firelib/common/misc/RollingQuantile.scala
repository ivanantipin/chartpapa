package firelib.common.misc

import java.util
import java.util.Comparator




class  RollingQuantile(val quantile: Double, val length: Int) {

    case class Node(val value: Double, var isLeft: Boolean, val counter : Int)

    class NodeComparator extends Comparator[Node]{
        override def compare(o1: Node, o2: Node): Int = {
            val cmp = o1.value.compareTo(o2.value)
            if (cmp != 0) {
                return cmp;
            }
            return o1.counter - o2.counter
        }
    }

    private val queue = new util.LinkedList[Node]()

    private val left = new util.TreeSet[Node](new NodeComparator)
    private val right = new util.TreeSet[Node](new NodeComparator)

    private var cnt = 0

    def count = cnt

    def value: Double = {
        if (left.isEmpty && right.isEmpty) {
            return 0
        }
        if (left.isEmpty) {
            return right.first().value
        }
        if (right.isEmpty) {
            return left.last().value
        }
        return (left.last().value + right.first().value) / 2
    }

    def addMetric(m: Double) = {
        assert(!m.isNaN && !m.isInfinite)
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

        if (m > value) {
            val n = new Node(m, false, cnt)
            queue.add(n)
            right.add(n)
        }
        else {
            val n = new Node(m, true, cnt)
            queue.add(n)
            left.add(n)
        }
        balance()
    }

    private def balance() {
        val diff = left.size / quantile - right.size / (1 - quantile)
        if (diff > 0) {
            val amDiff = (left.size - 1) / quantile - (right.size + 1) / (1 - quantile)
            if (math.abs(amDiff) < math.abs(diff)) {
                val leftMax = left.last()
                assert(left.remove(leftMax))
                right.add(leftMax)
                leftMax.isLeft = false

            }
        }
        else {
            val amDiff = (left.size + 1) / quantile - (right.size - 1) / (1 - quantile)
            if (math.abs(amDiff) < math.abs(diff)) {
                val rightMin = right.first()
                assert(right.remove(rightMin))
                left.add(rightMin)
                rightMin.isLeft = true
            }
        }
    }
}
