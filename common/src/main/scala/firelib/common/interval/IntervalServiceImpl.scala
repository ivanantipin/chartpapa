package firelib.common.interval

import java.time.Instant

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


class IntervalServiceImpl extends IntervalService {

    val nodes = mutable.TreeSet.empty(Ordering.by[Node,Long](a=>a.interval.durationMs))

    var rootNode : Node =_

    def rootInterval : Interval = rootNode.interval

    class Node(val interval : Interval){
        val childs = new ArrayBuffer[Node](3)
        val listeners = new ArrayBuffer[Instant => Unit](3)

        def onStep(ms : Long, dt : Instant) : Unit = {
            if (ms  % interval.durationMs == 0) {
                listeners.foreach(_(dt))
                if(childs.nonEmpty)
                    childs.foreach(_.onStep(ms,dt))
            }
        }
    }
    def addListener(interval: Interval, action: Instant  => Unit, atTheBeginning : Boolean = false) = {
        if(atTheBeginning){
            addOrGetIntervalNode(interval).listeners.insert(0,action)
        }else{
            addOrGetIntervalNode(interval).listeners += action
        }
        rebuildTree()
    }

    def rebuildTree(): Unit ={
        nodes.foreach(_.childs.clear())

        rootNode = nodes.head

        var nodesResult = List(nodes.head)

        nodes.toStream.drop(1).foreach(nd=>{
            nodesResult.find(p=>depends(p.interval,nd.interval)) match {
                case Some(par) => par.childs += nd
                case None => throw new RuntimeException("no parent found!!")
            }
            nodesResult = nd +: nodesResult
        })

    }

    def addOrGetIntervalNode(interval: Interval): Node = {
        nodes.find(n => n.interval == interval) match {
            case Some(node) => node
            case None => {
                val node = new Node(interval)
                nodes += node
                node
            }
        }

    }

    def depends(parent : Interval, child : Interval) : Boolean = {
        child.durationMs % parent.durationMs == 0
    }

    def removeListener(interval: Interval, action: Instant  => Unit): Unit = {
        addOrGetIntervalNode(interval).listeners -= action
    }

    def onStep(dt:Instant) = {
        rootNode.onStep(dt.toEpochMilli,dt)
    }
}
