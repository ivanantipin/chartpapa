package firelib.common

import java.time.Instant

import firelib.common.agenda.AgendaComponent
import firelib.common.timeservice.TimeServiceManagedComponent
import org.junit.{Assert, Test}

import scala.collection.mutable.ArrayBuffer

class AgendaCompTest {


    @Test
    def testAgenda(): Unit ={

        val agenda = new AgendaComponent with TimeServiceManagedComponent

        val lst = new ArrayBuffer[Int]()

        val now: Instant = Instant.now()

        agenda.agenda.addEvent(now.plusMillis(1),()=>{
            lst += 2
        },0)

        agenda.agenda.addEvent(now,()=>{
            lst += 0
        },0)
        agenda.agenda.addEvent(now,()=>{
            lst += 1
        },1)


        agenda.agenda.next()
        agenda.agenda.next()
        agenda.agenda.next()

        Assert.assertEquals(new ArrayBuffer[Int]() :+ 0 :+ 1 :+ 2, lst)

    }

}
