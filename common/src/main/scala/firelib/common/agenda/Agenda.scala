package firelib.common.agenda

import java.time.Instant

trait Agenda {
    def next(): Unit

    def addEvent(time: Instant, act: () => Unit, prio : Int): Unit

}
