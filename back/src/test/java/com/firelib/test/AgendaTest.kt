package com.firelib.test

import firelib.common.agenda.AgendaImpl
import org.junit.Assert
import org.junit.Test
import java.time.Instant

class AgendaCompTest {


    @Test
    fun testAgenda(): Unit {

        val agenda = AgendaImpl()

        val lst = ArrayList<Int>()

        val now: Instant = Instant.now()

        agenda.execute(now.plusMillis(1), {
            lst += 2
        }, 0)

        agenda.execute(now, {
            lst += 0
        }, 0)

        agenda.execute(now, {
            lst += 1
        }, 1)


        agenda.next()
        agenda.next()
        agenda.next()

        Assert.assertEquals(arrayListOf(0, 1, 2), lst)
    }
}
