package firelib.common.misc

interface Channel<T> : PubChannel<T>, SubChannel<T> {}

fun <T> makeChannel(subs: (T) -> Unit): Channel<T> {
    val ret = NonDurableChannel<T>()
    ret.subscribe(subs)
    return ret
}

interface PubChannel<T> {
    fun publish(t: T): Unit
}

interface SubChannel<T> {
    fun subscribe(lsn: (T) -> Unit): ChannelSubscription

    fun <B> lift(mf: (T) -> List<B>): SubChannel<B> {
        val ret = NonDurableChannel<B>()
        subscribe {
            mf(it).forEach { oo ->
                run {
                    ret.publish(oo)
                }
            }
        }
        return ret
    }

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

