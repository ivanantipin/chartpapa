package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.flattenAll
import firelib.core.misc.atMoscow
import firelib.core.store.DbMapper
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
import java.time.Instant
import java.time.LocalDate


class TrendModel(context: ModelContext, val props: Map<String, String>) : TrendModelMBean, Model(context, props) {

    val daytss = enableSeries(Interval.Day)

    val nonInterpolated = enableSeries(Interval.Day, interpolated = false)


    init {

        enableSeries(Interval.Min60, interpolated = false)[0].preRollSubscribe {
            if (daytss[0].count() > 40 && currentTime().atMoscow().hour == 18) {
                val back = props["period"]!!.toInt()

                val num = props["number"]!!.toInt()

                val idxToRet = daytss.mapIndexed { idx, ts ->
                    Pair(idx, (ts[0].close - nonInterpolated[idx][back].close) / nonInterpolated[idx][back].close)
                }


                val indexed = idxToRet.filter { it.second.isFinite() && it.second > 0 }

                val sortedBy = indexed.sortedBy { -it.second }
                val sorted = sortedBy.subList(0, Math.min(num, indexed.size)).map { it.first }

                if(Instant.now().epochSecond - currentTime().epochSecond < 24*3600){
                    idxToRet.forEach {
                        log.info("return for ticker ${instruments()[it.first]} is ${it.second}")
                    }

                    log.info("=====")

                    log.info("top is ${sorted}")
                }


                oms.forEachIndexed { idx, om ->
                    if (sorted.contains(idx)) {
                        longForMoneyIfFlat(idx, 15000)
                    } else {
                        om.flattenAll()
                    }
                }

            }
        }
    }

    override fun buy(ticker: String) {
        val idx = context.config.instruments.indexOfFirst { it.equals(ticker, true) }
        if (idx >= 0) {
            longForMoneyIfFlat(idx, 1000)
        }
    }

    override fun sell(ticker: String) {
        val idx = context.config.instruments.indexOfFirst { it.equals(ticker, true) }
        if (idx >= 0) {
            oms[idx].flattenAll("mbean")
        }
    }
}

fun trendModelConfig(): ModelBacktestConfig {
    return ModelBacktestConfig(TrendModel::class).apply {
        instruments = tickers
//        tickerToDiv = DivHelper.getDivs()
        startDate(LocalDate.now().minusDays(200))
        param("period", 33)
        param("number", 5)
    }
}

fun main() {
    val storageImpl = MdStorageImpl()


    val mapper = DbMapper(finamMapperWriter(), { it.market == "1" })

//    tickers.forEach {
//        storageImpl.updateMarketData(mapper(it.toUpperCase()))
//    }


    val conf = trendModelConfig()
    conf.runStrat()
}
