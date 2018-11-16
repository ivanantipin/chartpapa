package firelib.indicators

import java.time.Instant
import java.util
import java.util.Comparator

class Donchian (val windowSec : Int, val useMax : Boolean)  {

    case class Node(val value: Double, val time : Instant)

    class NodeComparator extends Comparator[Node]{
        override def compare(o1: Node, o2: Node): Int = {
            val cmp = o1.value.compareTo(o2.value)
            if (cmp != 0) {
                return cmp;
            }
            return o1.time.compareTo(o2.time)
        }
    }

    private val queue = new util.LinkedList[Node]()

    private val tree = new util.TreeSet[Node](new NodeComparator)

    private var cnt = 0

    def nextCount : Int = {cnt +=1; cnt}

    def max : Double = tree.last().value

    def min : Double = tree.first().value

    def value = if(useMax) max else min

    def addMetric(t : Instant, m: Double) = {
        val node: Node = new Node(m, t)
        queue.add(node)
        tree.add(node)
        val head = queue.getLast.time
        while(head.getEpochSecond - queue.getFirst.time.getEpochSecond > windowSec){
            val nn =  queue.poll()
            tree.remove(nn)
        }
    }
}
