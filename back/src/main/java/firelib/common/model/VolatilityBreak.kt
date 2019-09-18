package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.runStrat
import firelib.common.interval.Interval
import firelib.common.misc.Quantiles
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted
import firelib.indicators.ATR
import firelib.indicators.Donchian
import java.time.LocalDate


/*
research model for simple breakout after low volatility period
 */

class VolatilityBreak(context: ModelContext, properties: Map<String, String>) : Model(context, properties) {

    val period = 20

    init {

        val daytss = enableSeries(interval = Interval.Day,interpolated = false)

        val quantiles = context.instruments.map {
            Quantiles<Double>(1000);
        }
        val volumeQuantiles = context.instruments.map {
            Quantiles<Double>(100);
        }

        val mas = daytss.mapIndexed { idx, it ->
            val atr = ATR(period, it)
            it.preRollSubscribe {
                quantiles[idx].add(atr.value())
            }
            atr
        }

        val donchians = daytss.map { Donchian(period) }


        enableFactor("volatility", {
            val ret = quantiles[it].getQuantile(mas[it].value())
            if (ret.isNaN()) 0.5 else ret
        })
        enableFactor("volume", {
            val ret = volumeQuantiles[it].getQuantile(daytss[it].last().volume.toDouble())
            if (ret.isNaN()) 0.5 else ret
        })

        enableFactor("volume1", {
            val ret = volumeQuantiles[it].getQuantile(daytss[it][1].volume.toDouble())
            if (ret.isNaN()) 0.5 else ret
        })

        daytss.forEachIndexed({ idx, it ->
            it.preRollSubscribe {
                if (it.count() > period) {

                    val vola = quantiles[idx].getQuantile(mas[idx].value())
                    val vol = volumeQuantiles[idx].getQuantile(daytss[idx].last().volume.toDouble())

                    if (it[0].close > donchians[idx].max && vola < 0.75 && vol > 0.8) {
                        buyIfNoPosition(idx, 1000_000)
                    }
                }
                volumeQuantiles[idx].add(it[0].volume.toDouble())
                donchians[idx].add(it[0])
            }
        })

        closePositionByTimeout(days = properties["hold_days"]!!.toInt())
    }
}

suspend fun main(args : Array<String>) {

    val divs = DivHelper.getDivs()

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    val conf = ModelBacktestConfig().apply {

        reportTargetPath = "/home/ivan/projects/chartpapa/market_research/vol_break_report"

        endDate(LocalDate.of(2016, 1, 1))

        opt("hold_days", 3, 30, 2)

        instruments = divs.keys.map { instr ->
            InstrumentConfig(instr, { ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!) })
        }
    }

    conf.runStrat { context, props ->
        VolatilityBreak(context, props)
    }
}