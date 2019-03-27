package firelib.common.misc

import java.util.*

class DurableChannel<T> : Channel<T> {
    val listeners = LinkedList<(T) -> Unit>()
    val msgs = LinkedList<T>()

    override fun publish(t: T): Unit {
        listeners.forEach { it(t) }
        msgs += t
    }

    override fun subscribe(lsn: (T) -> Unit): ChannelSubscription {
        listeners += lsn
        msgs.forEach { lsn(it) }
        return object : ChannelSubscription {
            override fun unsubscribe() {
                listeners -= lsn
            }
        }
    }
}