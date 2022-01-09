package firelib.model

import com.firelib.techbot.TdLineDated
import com.firelib.techbot.TrendsCreator
import com.firelib.techbot.chart.RenderUtils
import com.firelib.techbot.chart.TrendLinesRenderer
import com.firelib.techbot.domain.LineType
import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.atMoscow
import firelib.core.timeseries.toSequence
import firelib.model.prod.factorBarQuantLow
import firelib.model.prod.factorReturn
import firelib.model.prod.factorVolume
import firelib.model.prod.factorWeekday
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate


data class DemarkContext(val line : TdLineDated)

class DemarkLines(context: ModelContext, val props: Map<String, String>) : Model(context, props) {
    init {

        val cnt = 100

        val dayTss = enableSeries(Interval.Day, 2*cnt + 10, false)
        val daily = enableSeries(Interval.Day)[0]

        val retFac = factorReturn()
        factorWeekday()
        factorBarQuantLow()
        factorVolume()

        var demarkContext : DemarkContext? = null

        enableTradeContextSupplier { idx->
            val locCtx = demarkContext
            if(locCtx == null){
                {a,b-> ByteArray(0) }
            }else{
                {openTrade, closingTrade->
                    val ohlcs = closingTrade.tradeCtxValue as List<Ohlc>
                    val start = ohlcs.indexOfFirst { it.endTime.toEpochMilli() == locCtx.line.x0 }
                    val toh = if(start == -1) ohlcs else ohlcs.subList(start, ohlcs.size)
                    val opts = TrendLinesRenderer.trendLinesWithoutBySell(toh, "Xxx", listOf(locCtx.line.toTdLine(toh)))
                    opts.annotations += RenderUtils.buySells(listOf(openTrade,closingTrade))
                    Json { prettyPrint = true }.encodeToString( opts).toByteArray()
                }
            }
        }

        enableTradeContext { idx->
            if(dayTss[0].count() > 2*cnt){
                dayTss[0].toSequence(2*cnt).toList()
            }else{
                dayTss[0].toSequence(dayTss[0].count() - 1).toList()
            }
        }

        enableFactor("distance"){
            if(demarkContext != null){
                (currentTime().toEpochMilli() - demarkContext!!.line.x1)/1000.0/3600.0
            }else{
                -1.0
            }

        }

        enableSeries(Interval.Min10)[0].preRollSubscribe {
            if (currentTime().atMoscow().hour == 18 && !daily[0].interpolated) {

                if(currentTime().atMoscow().minute == 40){

                    val ts = dayTss[0]

                    if(ts.count() > 2*cnt){
                        val closes = ts.toSequence(cnt).map { it.close }.toList()
                        val highs = ts.toSequence(cnt).map { it.high }.toList()
                        val lows = ts.toSequence(cnt).map { it.low }.toList()
                        val hpivots = TrendsCreator.findSimplePivots(highs, 4, LineType.Resistance)

                        val res = TrendsCreator.make2Lines(hpivots, highs, lows, LineType.Resistance)


                        res.forEach { tdLine ->
                            if(tdLine.intersectPoint == null && tdLine.calcValue(closes.size) <  daily[0].close ){
                                if(tdLine.y0 > tdLine.y1){
                                    val ohlcs = ts.toSequence(cnt).toList()
                                    demarkContext = DemarkContext(tdLine.toDated(ohlcs))
                                    longForMoneyIfFlat(0, 1000_000)
                                }
                            }
                        }
                    }
                }
            }
        }

        closePosByCondition { idx->
            retFac(idx) < 0.2
        }

    }
}

fun main() {

    ModelConfig(DemarkLines::class).runStrat(ModelBacktestConfig().apply {
        interval = Interval.Min10
        histSourceName = SourceName.FINAM
        instruments = listOf("Si")
        maxRiskMoneyPerSec = 1000_0000
        startDate(LocalDate.now().minusDays(4000))
//        endDate(LocalDate.now().minusDays(300))
    })
}