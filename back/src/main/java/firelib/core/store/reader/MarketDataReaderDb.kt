package firelib.core.store.reader

import firelib.core.store.MdDao
import firelib.core.misc.atUtc
import firelib.core.domain.Ohlc
import java.time.Instant

class MarketDataReaderDb(val dao : MdDao, val ticker : String, val endTime : Instant, val waitOnEnd : Boolean = true) : MarketDataReader<Ohlc> {

    var cind = 0;

    val ohlcs = mutableListOf<Ohlc>()

    init {
        ohlcs += dao.queryAll(ticker)
    }

    override fun seek(time: Instant): Boolean {
        val idx = ohlcs.indexOfFirst { it.endTime.isAfter(time) }
        cind = idx
        return idx > 0
    }

    override fun current(): Ohlc {
        return ohlcs[cind]
    }

    override fun isReadable(): Boolean {
        return cind < ohlcs.size
    }

    override fun read(): Boolean {
        if(++cind >= ohlcs.size){
            if(waitOnEnd){
                while (true){
                    val add = dao.queryAll(ticker, ohlcs.last().endTime.atUtc())
                    val ff = add.filter { ohlcs.last().endTime <= it.endTime }
                    if(ff.isEmpty()){
                        Thread.sleep(100000)
                    }else{
                        ohlcs += ff
                        break
                    }
                }
            }
        }
        return cind < ohlcs.size
    }

    override fun startTime(): Instant {
        return ohlcs[0].endTime
    }

    override fun endTime(): Instant {
        return endTime
    }

    override fun close() {
    }
}