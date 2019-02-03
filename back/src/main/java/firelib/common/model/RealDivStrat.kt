package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.ModelFactory
import firelib.common.core.runSimple
import firelib.common.interval.Interval
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.misc.atUtc
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.flattenAll
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneOffset


class RealDivFac : ModelFactory {
    override fun invoke(context: ModelContext, props: Map<String, String>): Model {
        return RealDivModel(context, props)
    }
}



class RealDivModel(val context: ModelContext, val props: Map<String, String>) : Model {
    val oman = makeOrderManagers(context)

    init {

        oman.forEach({

            val gen = StreamTradeCaseGenerator()

            it.tradesTopic().subscribe {

            val cases = gen(it)
            if(!cases.isEmpty()){

                cases.forEach({
                    println(it.first)
                    println(it.second)
                }
                )
            }
        }})

        val divMap = DivHelper.getDivs()
        val verbose = false

        context.instruments.forEachIndexed({ idx, instrument ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Min10, 100)
            val divs = divMap[instrument]!!


            var nextIdx = -2




            if(true){
                ret.preRollSubscribe {

                    if(nextIdx == -2){
                        nextIdx = divs.indexOfFirst {
                            it.date.atStartOfDay().isAfter(context.timeService.currentTime().atUtc())
                        }
                        if(nextIdx >=0){
                            //println("next div is ${divs[nextIdx]} for instrument ${instrument}" )
                        }

                    }

                    val time = ret[0].dtGmtEnd.atUtc()

                    val date = time.toLocalDate()

                    val localTime = time.toLocalTime()


                    if(verbose && oman[idx].position() != 0){
                        println("${ret[0]}")
                    }

                    if(localTime.hour == 18 && localTime.minute == 30 && nextIdx >= 0 && !ret[0].interpolated){

                        val nextDivDate = divs[nextIdx].date

                        fun check() : Boolean{
                            var dt = date.plusDays(1)
                            while (!dt.isAfter(nextDivDate)){
                                if(dt == nextDivDate){
                                    return true;
                                }
                                if(dt.dayOfWeek == DayOfWeek.SATURDAY || dt.dayOfWeek == DayOfWeek.SUNDAY){
                                    dt = dt.plusDays(1)
                                }else{
                                    return false
                                }
                            }
                            return false
                        }

                        if(check()){
                            val prevIdx = divs[nextIdx]
                            if(verbose){
                                println("entering for ${instrument} time ${context.timeService.currentTime()} div is $prevIdx price is ${ret[0].close}")
                                println("t0 ${ret[0]}")
                                println("t1 ${ret[1]}")
                            }

                            oman[idx].makePositionEqualsTo((100_000.0/ret[0].close).toInt())
                            nextIdx = divs.indexOfFirst {
                                it.date.isAfter(prevIdx.date) && it.date.atStartOfDay().isAfter(context.timeService.currentTime().atUtc())
                            }
                            if(nextIdx >0){
                                //println("next div is ${divs[nextIdx]} for instrument ${instrument}" )
                            }

                        }
                    }

                    if(localTime.hour == 18 && localTime.minute == 20 && oman[idx].position() != 0 && !ret[0].interpolated){
                        if(verbose){
                            println("exit position for ${instrument} time is ${context.timeService.currentTime()}  price is ${ret[0].close}")
                            println("t0 ${ret[0]}")
                            println("t1 ${ret[1]}")
                        }

                        oman[idx].flattenAll()
                    }
                }
            }else{
                ret.preRollSubscribe {
                    if(it[0].interpolated && !it[1].interpolated){
                        val time = ret[1].dtGmtEnd.atUtc()
                        val date = time.toLocalDate()
                        val localTime = time.toLocalTime()
                        if(verbose){
                            println("instrument ${instrument} date ${date} localtime ${localTime} minute ${time.minute} hour ${time.hour}")
                        }
                    }
                }
            }
        })
    }

    override fun properties(): Map<String, String> {
        return props
    }

    override fun orderManagers(): List<OrderManager> {
        return oman
    }

    override fun update() {}
}

suspend fun main(args: Array<String>) {

    val divs = DivHelper.getDivs()

    val conf = ModelBacktestConfig()
    conf.reportTargetPath = "./report/divsStrats"

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    conf.instruments = DivHelper.getDivs().keys.map {instr-> InstrumentConfig(instr, { time ->
        val delegate = MarketDataReaderSql(mdDao.queryAll(instr))
        delegate
        ReaderDivAdjusted(delegate, divs[instr]!!.map { Pair(it.date.atStartOfDay().toInstant(ZoneOffset.UTC),it.div) })
    })
    }

    conf.precacheMarketData = false
    runSimple(conf, RealDivFac())
    println("done")

}
