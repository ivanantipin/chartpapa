package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.StreamTradeCaseGenerator
import firelib.core.misc.atUtc
import firelib.core.flattenAll
import firelib.core.misc.atMoscow
import java.time.LocalDate


class RealDivModel(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    init {
        orderManagers().forEach {
            val gen = StreamTradeCaseGenerator()
            it.tradesTopic().subscribe {
                val cases = gen.genClosedCases(it)
                if (!cases.isEmpty()) {
                    cases.forEach {
                        log.info("${it.first}")
                        log.info("${it.second}")
                    }
                }
            }
        }

        val divMap = DivHelper.getDivs()

        val tss = enableSeries(Interval.Min10)

        context.config.instruments.forEachIndexed { idx, instrument ->
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
                            //log.info("next div is ${divs[nextIdx]} for instrument ${instrument}" )
                        }

                    }

                    val date = currentTime().atMoscow().toLocalDate()

                    val localTime = currentTime().atMoscow()


                    if (context.config.verbose && orderManagers()[idx].position() != 0) {
                        log.info("${ret[0]}")
                    }

                    if (localTime.hour == 18 && localTime.minute == 30 && nextIdx >= 0 && !ret[0].interpolated) {

                        val nextDivDate = divs[nextIdx].lastDayWithDivs
                        if (date == nextDivDate) {
                            val prevIdx = divs[nextIdx]
                            if (context.config.verbose) {
                                log.info("entering for ${instrument} time ${context.timeService.currentTime()} div is $prevIdx price is ${ret[0].close}")
                                log.info("t0 ${ret[0]}")
                                log.info("t1 ${ret[1]}")
                            }

                            longForMoneyIfFlat(idx, 100_000)

                            nextIdx = divs.indexOfFirst {
                                it.lastDayWithDivs.isAfter(prevIdx.lastDayWithDivs) && it.lastDayWithDivs.atStartOfDay().isAfter(
                                    currentTime().atMoscow()
                                )
                            }
                            if (nextIdx > 0) {
                                //log.info("next div is ${divs[nextIdx]} for instrument ${instrument}" )
                            }

                        }
                    }

                    if (localTime.hour == 18 && localTime.minute == 20 && orderManagers()[idx].position() != 0 && !ret[0].interpolated) {
                        if (context.config.verbose) {
                            log.info("exit position for ${instrument} time is ${context.timeService.currentTime()}  price is ${ret[0].close}")
                            log.info("t0 ${ret[0]}")
                            log.info("t1 ${ret[1]}")
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
                            log.info("instrument ${instrument} date ${date} localtime ${localTime} minute ${time.minute} hour ${time.hour}")
                        }
                    }
                }
            }
        }
    }
}

fun main() {
    val conf = ModelBacktestConfig(RealDivModel::class).apply {
        instruments = DivHelper.getDivs().keys.toList()
        tickerToDiv = DivHelper.getDivs()
        startDate(LocalDate.now().minusDays(300))
    }

    conf.runStrat()
}
