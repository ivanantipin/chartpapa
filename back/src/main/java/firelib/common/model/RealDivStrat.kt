package firelib.common.model

import com.funstat.domain.InstrId
import com.funstat.finam.FinamDownloader
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.instruments
import firelib.common.config.runStrat
import firelib.common.interval.Interval
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.misc.atUtc
import firelib.common.ordermanager.flattenAll


class RealDivModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        orderManagers().forEach({
            val gen = StreamTradeCaseGenerator()
            it.tradesTopic().subscribe {
                val cases = gen.genClosedCases(it)
                if (!cases.isEmpty()) {
                    cases.forEach({
                        println(it.first)
                        println(it.second)
                    }
                    )
                }
            }
        })

        val divMap = DivHelper.getDivs()

        val tss = enableSeries(Interval.Min10)

        context.tickers().forEachIndexed({ idx, instrument ->
            val divs = divMap[instrument]!!
            val ret = tss[idx]

            var nextIdx = -2

            if (true) {
                ret.preRollSubscribe {

                    if (nextIdx == -2) {
                        nextIdx = divs.indexOfFirst {
                            it.lastDayWithDivs.atStartOfDay().isAfter(context.timeService.currentTime().atUtc())
                        }
                        if (nextIdx >= 0) {
                            //println("next div is ${divs[nextIdx]} for instrument ${instrument}" )
                        }

                    }

                    val time = ret[0].endTime.atUtc()

                    val date = time.toLocalDate()

                    val localTime = time.toLocalTime()


                    if (context.config.verbose && orderManagers()[idx].position() != 0) {
                        println("${ret[0]}")
                    }

                    if (localTime.hour == 18 && localTime.minute == 30 && nextIdx >= 0 && !ret[0].interpolated) {

                        val nextDivDate = divs[nextIdx].lastDayWithDivs
                        if (date == nextDivDate) {
                            val prevIdx = divs[nextIdx]
                            if (context.config.verbose) {
                                println("entering for ${instrument} time ${context.timeService.currentTime()} div is $prevIdx price is ${ret[0].close}")
                                println("t0 ${ret[0]}")
                                println("t1 ${ret[1]}")
                            }

                            buyIfNoPosition(idx,100_000)

                            nextIdx = divs.indexOfFirst {
                                it.lastDayWithDivs.isAfter(prevIdx.lastDayWithDivs) && it.lastDayWithDivs.atStartOfDay().isAfter(context.timeService.currentTime().atUtc())
                            }
                            if (nextIdx > 0) {
                                //println("next div is ${divs[nextIdx]} for instrument ${instrument}" )
                            }

                        }
                    }

                    if (localTime.hour == 18 && localTime.minute == 20 && orderManagers()[idx].position() != 0 && !ret[0].interpolated) {
                        if (context.config.verbose) {
                            println("exit position for ${instrument} time is ${context.timeService.currentTime()}  price is ${ret[0].close}")
                            println("t0 ${ret[0]}")
                            println("t1 ${ret[1]}")
                        }

                        orderManagers()[idx].flattenAll()
                    }
                }
            } else {
                ret.preRollSubscribe {
                    if (it[0].interpolated && !it[1].interpolated) {
                        val time = ret[1].endTime.atUtc()
                        val date = time.toLocalDate()
                        val localTime = time.toLocalTime()
                        if (context.config.verbose) {
                            println("instrument ${instrument} date ${date} localtime ${localTime} minute ${time.minute} hour ${time.hour}")
                        }
                    }
                }
            }
        })
    }
}

fun main() {

    val conf = ModelBacktestConfig(RealDivModel::class).apply {
        instruments(DivHelper.getDivs().keys.map { InstrId.dummyInstrument(it) }, FinamDownloader.SOURCE)
    }

    conf.runStrat()
}
