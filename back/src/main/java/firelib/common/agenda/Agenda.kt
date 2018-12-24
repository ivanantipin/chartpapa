package firelib.common.agenda

import java.time.Instant

interface Agenda {
    fun next(): Unit

    fun execute(time: Instant, act: () -> Unit, prio : Int): Unit

}