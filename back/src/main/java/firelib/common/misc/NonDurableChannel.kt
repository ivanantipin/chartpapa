package firelib.common.misc


interface ChannelSubscription{
    fun unsubscribe()
}

class NonDurableChannel<T> : Channel<T>{

    val listeners = ArrayList<(T)->Unit>()

    override fun publish(t : T) : Unit {
        listeners.forEach {it(t)}
    }

    override fun subscribe(lsn : (T)->Unit) : ChannelSubscription {
        listeners += lsn
        return object: ChannelSubscription{
            override fun unsubscribe() {
                listeners -= lsn
            }
        }
    }

}