package firelib.common.misc

import scala.collection.mutable.ArrayBuffer

class DurableChannel[T] extends Channel[T]{

    val listeners = new ArrayBuffer[T=>Unit]()

    val msgs = new ArrayBuffer[T](2)

    def publish(t : T) : Unit = {
        listeners.foreach(_(t))
        msgs += t
    }

    def subscribe(lsn : T=>Unit) : ChannelSubscription= {
        listeners += lsn
        msgs.foreach(lsn(_))
        new ChannelSubscription {
            override def unsubscribe(): Unit = listeners -= lsn
        }

    }
}
