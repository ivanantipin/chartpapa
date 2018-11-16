package firelib.common.misc

trait Channel[T] extends PubChannel[T] with SubChannel[T] { }

object Channel {
    def apply[T](subs : T=>Unit) : Channel[T] = {
        val ret = new NonDurableChannel[T]
        ret.subscribe(subs)
        ret
    }
}

trait PubChannel[T]{
    def publish(t : T) : Unit
}

trait SubChannel[T]{
    def subscribe(lsn : T=>Unit) : ChannelSubscription

    def lift[B](mf : T=>Seq[B]) : SubChannel[B] = {
        val ret = new NonDurableChannel[B]
        subscribe(t=>{
            for(oo<-mf(t)){
                ret.publish(oo)
            }
        })
        ret
    }

    def map[B](mf : T=>B) : SubChannel[B] = {
        val ret = new NonDurableChannel[B]
        subscribe(t=>ret.publish(mf(t)))
        ret
    }

    def filter(flt: (T) => Boolean): SubChannel[T] = {
        val ret = new NonDurableChannel[T]()
        subscribe(tt=>{
            if(flt(tt)){
                ret.publish(tt)
            }
        })
        ret
    }


}


