package firelib.common.interval

import java.time.Instant
import java.util.*


class IntervalServiceImpl : IntervalService {

    val nodes = TreeSet<Node> { a, b->a.interval.durationMs.compareTo(b.interval.durationMs)}

    var rootNode : Node = Node(Interval.Min1)

    override fun rootInterval (): Interval = rootNode.interval

    class Node(val interval : Interval){
        val childs = ArrayList<Node>(3)
        val listeners = ArrayList<(Instant) -> Unit>(3)

        fun onStep(ms : Long, dt : Instant) : Unit {
            if (ms  % interval.durationMs == 0L) {
                listeners.forEach {it(dt)}
                if(childs.isNotEmpty())
                    childs.forEach {it.onStep(ms,dt)}
            }
        }
    }
    override fun addListener(interval: Interval, action: (Instant)  -> Unit) {
        addOrGetIntervalNode(interval).listeners += action
        rebuildTree()
    }

    fun rebuildTree(): Unit {
        nodes.forEach {it.childs.clear()}

        rootNode = nodes.first()

        var nodesResult = listOf(nodes.first())

        nodes.stream().skip(1).forEach { nd->
            nd.childs.clear()
            val find = nodesResult.find { p -> depends(p.interval, nd.interval) }
            if(find != null){
                find.childs += nd
            }else{
                throw RuntimeException("no parent found!!")
            }
            nodesResult = arrayListOf(nd) + nodesResult
        }

    }

    private fun addOrGetIntervalNode(interval: Interval): Node {
        var ret = nodes.find { n -> n.interval == interval }
        if(ret == null) {
            ret = Node(interval)
            nodes += ret
        }
        return ret;
    }

    fun depends(parent : Interval, child : Interval) : Boolean {
        return child.durationMs % parent.durationMs == 0L
    }

    fun removeListener(interval: Interval, action: (Instant)  -> Unit): Unit {
        addOrGetIntervalNode(interval).listeners -= action
    }

    fun onStep(dt:Instant) {
        rootNode.onStep(dt.toEpochMilli(),dt)
    }
}