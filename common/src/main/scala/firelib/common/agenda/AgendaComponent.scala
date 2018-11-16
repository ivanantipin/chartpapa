package firelib.common.agenda

import java.time.Instant
import java.util.{Comparator, PriorityQueue}

import firelib.common.timeservice.TimeServiceManagedComponent


trait AgendaComponent {

    this : TimeServiceManagedComponent =>

    val agenda = new AgendaImpl

    class AgendaImpl extends Agenda{

        case class Rec(time : Instant, prio : Int, act : ()=>Unit)

        val comparator = new Comparator[Rec] {
            override def compare(o1: Rec, o2: Rec): Int = {
                val ret = o1.time.compareTo(o2.time)
                if(ret != 0){
                    return ret
                }
                return o1.prio.compareTo(o2.prio)
            }
        }

        val events = new PriorityQueue[Rec](comparator)

        def next() : Unit = {
            val ev = events.poll()
            timeServiceManaged.dtGmt = ev.time
            ev.act()
        }

        def addEvent(time : Instant, act : ()=>Unit, prio : Int) = {
            events.add(new Rec(time, prio,act))
        }
    }
}
