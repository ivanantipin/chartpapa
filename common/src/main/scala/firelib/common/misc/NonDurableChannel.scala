package firelib.common.misc

import scala.collection.mutable.ArrayBuffer


trait ChannelSubscription{
    def unsubscribe()
}

class NonDurableChannel[T] extends Channel[T]{

    val listeners = new ArrayBuffer[T=>Unit]()

    def publish(t : T) : Unit = {
        listeners.foreach(_(t))
    }

    def subscribe(lsn : T=>Unit) : ChannelSubscription = {
        listeners += lsn
        new ChannelSubscription {
            override def unsubscribe(): Unit = listeners -= lsn
        }
    }

}
