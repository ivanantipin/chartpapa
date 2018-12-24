package firelib.common.misc

class DurableChannel<T> : Channel<T> {
    val listeners = ArrayList<(T) -> Unit>()
    val msgs = ArrayList<T>(2)

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