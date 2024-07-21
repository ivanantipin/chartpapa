package com.firelib.techbot

import com.firelib.techbot.divreader.Div
import com.firelib.techbot.divreader.DivReader
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.staticdata.InstrIdDao
import com.firelib.techbot.staticdata.InstrumentsService
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdDaoContainer
import firelib.core.store.SourceFactory
import firelib.finam.MoexSourceAsync
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

class TopCalculator(val ohlcsService: OhlcsService, val instrumentsService: InstrumentsService) {

    val divs = ConcurrentHashMap<InstrId, List<Div>>()

    val log = LoggerFactory.getLogger(javaClass)

    var lastDivUpdated = 0L

    var instrs: List<InstrId> = emptyList()

    init {
        runBlocking {
            val preInstr = instrumentsService.id2inst.values.filter { it.source == "MOEX" }
            instrs = preInstr.filter {
                try {
                    val days = ohlcsService.getOhlcsForTf(it, Interval.Day)
                    val tooOld = System.currentTimeMillis() - days.last().endTime.toEpochMilli() > 4*24*3600_000
                    if(tooOld){
                        println("instrument is filtered out because last update is too old ${days.last().endTime} " )
                    }
                    val volume = days.last().volume * days.last().close
                    val ret = days.isNotEmpty() && volume > 300_000_000
                    if(!ret){
                        println("instrument is filtered out because volume ${volume/1000_000} mils is less than enough" )
                    }
                    ret &&  !tooOld
                } catch (e: Exception) {
                    false;
                }
            }
            instrs.forEach { updateDiv(it) }
        }

    }


    private fun updateDiv(it: InstrId) {
        try {
            divs[it] = DivReader.fetchDivs(it.code)
            log.info("divs fetched for ${it.code}")
        } catch (e: Exception) {
            divs[it] = emptyList()
            log.error("failed to update ticker {}", it.code, e)
        }
    }

    fun calculate(lastDays: Int, top : Int): List<Pair<InstrId, Double>> {
        val ret = instrs.map {
            runBlocking {
                ohlcsService.initTimeframeIfNeeded(it);

                calculate(it, lastDays)

            }
        }.sortedBy { it.second }
        return ret.takeLast(top)

    }

    suspend fun calc(instr : String, lastDays: Int) : Pair<InstrId, Double>{
        val find = instrs.find { it.code == instr }!!
        return calculate(find, lastDays)
    }

    private suspend fun calculate(
        it: InstrId,
        lastDays: Int
    ): Pair<InstrId, Double> {
        val ohlcsForTf = ohlcsService.getOhlcsForTf(it, Interval.Day)
        if (ohlcsForTf.size < lastDays) {
            return it to -1000.0
        }
        val ohlc = ohlcsForTf[ohlcsForTf.size - lastDays]

        val adjust = divs[it]!!.filter {
            it.lastDayWithDivs.toInstantDefault() > ohlc.endTime
        }.sumOf { it.div }
        val last = ohlcsForTf.last()


        if (ohlc.volume * ohlc.close < 200_000_000) {
            return it to -1000.0
        }

        val ret = (adjust + last.close - ohlc.close) / ohlc.close
        return it to ret

    }

}

suspend fun main(args: Array<String>) {
    DbHelper.initDefaultDb()

    val service = OhlcsService(MdDaoContainer(), SourceFactory())

    val calculator = TopCalculator(service, InstrumentsService(InstrIdDao()))
    //calculator.calc("TUZA", 37)
    val cc = calculator.calculate(37, 5)
    cc.forEach {
        println(it)
    }


}
