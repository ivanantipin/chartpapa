package firelib.common.agenda


import firelib.common.timeservice.TimeServiceManaged
import java.time.Instant
import java.util.*


class AgendaImpl(val timeService : TimeServiceManaged) : Agenda{

    data class Rec(val time : Instant, val prio : Int, val act : ()->Unit)

    val comparator = object:Comparator<Rec> {
        override fun compare(o1: Rec, o2: Rec): Int {
            val ret = o1.time.compareTo(o2.time)
            if(ret != 0){
                return ret
            }
            return o1.prio.compareTo(o2.prio)
        }
    }

    val events = PriorityQueue<Rec>(comparator)

    override fun next() : Unit {
        val ev = events.poll()
        timeService.updateTime(ev.time)
        ev.act()
    }

    override fun addEvent(time : Instant, act : ()->Unit, prio : Int) {
        events.add(Rec(time, prio,act))
    }
}
