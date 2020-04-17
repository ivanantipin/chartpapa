package firelib.core.misc

import java.util.*


interface ChannelSubscription{
    fun unsubscribe()
}

class NonDurableChannel<T> : Channel<T>{

    val listeners = LinkedList<(T)->Unit>()

    override fun publish(t : T) {
        listeners.forEach {
            try{
                it(t)
            }catch (e : Exception){
                //fixme switch to log
                e.printStackTrace()
            }
        }
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