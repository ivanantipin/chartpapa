package firelib.core.misc

import java.util.*

class DurableChannel<T> : Channel<T> {
    val listeners = LinkedList<(T) -> Unit>()
    val msgs = LinkedList<T>()

    override fun publish(t: T) {
        msgs += t
        listeners.forEach { it(t) }
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