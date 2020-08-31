package firelib.model

import firelib.core.Model
import firelib.core.ModelContext
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.enableSeries
import firelib.core.report.dao.GeGeWriter
import firelib.indicators.ZigZag
import java.time.LocalDate


data class ZiggiStat(val volume0 : Long, val volume1 : Long, val volumeUp0 : Long, val volumeUp1 : Long)


class ZigZagInvest(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val lst = mutableListOf<ZiggiStat>()

    init {
        val ration = 0.05
        enableSeries(Interval.Min10).forEach {
            val zigZag = ZigZag(ration)
            zigZag.addListener {
//                val first = it.first().low
//                val last = it.last().high
//                val diff = last - first
//                if(diff > 0){
//                    println("ratio is ${(last - first) / first}")
//                    val idx = it.indexOfLast {  (it.high - first) <  diff/2}
//                    if(idx > 0){
//                        val v0 = it.subList(0, idx).map { it.volume }.sum()
//                        val v1 = it.subList(idx, it.size).map { it.volume }.sum()
//                        val vUp0 = it.subList(0, idx).filter { it.isUpBar() }. map { it.volume }.sum()
//                        val vUp1 = it.subList(idx, it.size).filter { it.isUpBar() }.map { it.volume }.sum()
//
//                        lst += ZiggiStat(v0, v1, vUp0, vUp1)
//
//                    }else{
//                        println("idx < 0")
//                    }
//                }
            }
            it.preRollSubscribe {
                if(!it[0].interpolated){
                    zigZag.addOhlc(it[0])
                }
            }
        }
    }

    override fun onBacktestEnd() {
        val writer = GeGeWriter<ZiggiStat>(context.runConfig.getReportDbFile(), ZiggiStat::class)
        writer.write(lst)
    }

    companion object {
        //MdStorageImpl().updateMarketData(InstrId(code = "ALLFUTSi", source = SourceName.MT5.name), interval = Interval.Min15);
        fun modelConfig(tradeSize : Int = 100_000): ModelConfig {
            return ModelConfig(ZigZagInvest::class)
        }
    }
}

fun main() {
    ZigZagInvest.modelConfig().runStrat(ModelBacktestConfig().apply {
        instruments = tickers
        startDate(LocalDate.now().minusDays(3000))
    })
}