package firelib.common.reader

import com.funstat.store.MdDao
import firelib.common.misc.atUtc
import firelib.domain.Ohlc
import java.time.Instant

class MarketDataReaderDb(val dao : MdDao, val ticker : String, val endTime : Instant) : MarketDataReader<Ohlc> {

    var cind = 0;

    val ohlcs = mutableListOf<Ohlc>()

    init {
        ohlcs += dao.queryAll(ticker)
    }

    override fun seek(time: Instant): Boolean {
        val idx = ohlcs.indexOfFirst { it.dtGmtEnd.isAfter(time) }
        cind = idx
        return idx > 0
    }

    override fun current(): Ohlc {
        return ohlcs[cind]
    }

    override fun read(): Boolean {
        if(++cind >= ohlcs.size){
            while (true){
                val add = dao.queryAll(ticker, ohlcs.last().dtGmtEnd.atUtc())
                val ff = add.filter { ohlcs.last().time() <= it.time()  }
                if(ff.isEmpty()){
                    Thread.sleep(100000)
                }else{
                    ohlcs += ff
                    break
                }
            }
        }
        return cind < ohlcs.size
    }

    override fun startTime(): Instant {
        return ohlcs[0].dtGmtEnd
    }

    override fun endTime(): Instant {
        return endTime
    }

    override fun close() {
    }

}