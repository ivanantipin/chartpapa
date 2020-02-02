package firelib.core.misc

interface Channel<T> : PubChannel<T>, SubChannel<T> {}

interface PubChannel<T> {
    fun publish(t: T)
}

interface SubChannel<T> {
    fun subscribe(lsn: (T) -> Unit): ChannelSubscription

    fun <B> map(mf: (T) -> B): SubChannel<B> {
        val ret = NonDurableChannel<B>()
        subscribe { ret.publish(mf(it)) }
        return ret
    }

    fun filter(flt: (T) -> Boolean): SubChannel<T> {
        val ret = NonDurableChannel<T>()
        subscribe {
            if (flt(it)) {
                ret.publish(it)
            }
        }
        return ret
    }
}

