package firelib.core.store

import firelib.core.domain.Ohlc
import firelib.core.misc.atUtc
import firelib.core.store.reader.SimplifiedReader
import java.time.Instant
import java.util.*


class SimplifiedReaderImpl(val mdDao: MdDao, val code : String, val startTime : Instant) :
    SimplifiedReader {

    var lastRead : Instant = startTime

    var buffer  = LinkedList<Ohlc>()

    var done = false

    fun read(){
        val list = mdDao.queryAll(code, lastRead.atUtc(), 10_000)
        if(list.isNotEmpty()){
            buffer.addAll(list)
            lastRead = list.last().endTime
        }else{
            done = true
        }
    }

    override fun peek(): Ohlc? {
        if(done){
            return null
        }
        if(buffer.isEmpty()){
            read()
        }
        return buffer.peek()
    }

    override fun poll(): Ohlc {
        return buffer.poll()
    }

}