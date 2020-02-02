package com.firelib.stratserver

import com.firelib.Levels
import com.firelib.OhlcPeriod
import com.firelib.OhlcTO
import com.firelib.StratDescription
import firelib.core.SimpleRunCtx
import firelib.core.addModelWithDefaultParams
import firelib.core.domain.Interval
import firelib.core.mddistributor.MarketDataDistributor
import firelib.core.mddistributor.getOrCreatTss
import firelib.model.VolatilityBreak.Companion.modelConfig
import firelib.core.domain.Ohlc

fun convertOhlcToGrpc(ohlc: Ohlc, tkr: String, op: OhlcPeriod): OhlcTO {
    return OhlcTO.newBuilder().apply {
        open = ohlc.open
        high = ohlc.high
        low = ohlc.low
        close = ohlc.close
        timestamp = ohlc.endTime.toEpochMilli()
        ticker = tkr
        period = op

    }.build()
}


object ServiceFac {

    fun makeLevelsBc(distributor: MarketDataDistributor, tickers: List<String>): Broadcaster<Levels> {
        val levels = Broadcaster<Levels>("levels", historyKey = { l -> l.ticker })
        val dayTss = distributor.getOrCreatTss(Interval.Day, 2)
        val weekTss = distributor.getOrCreatTss(Interval.Week, 2)
        tickers.forEachIndexed({ idx, ticker ->
            val levelsGen = LevelsGen(levels, ticker)
            dayTss[idx].preRollSubscribe {
                levelsGen.onOhlc(it[0], Interval.Day)
            }
            weekTss[idx].preRollSubscribe {
                levelsGen.onOhlc(it[0], Interval.Week)
            }
        })
        return levels
    }

    fun makeInterServiceBc(mdDistributor: MarketDataDistributor, tickers: List<String>): Broadcaster<OhlcTO> {
        val defferer = Defferer()
        val dayTss = tickers.mapIndexed({ idx, ticker ->
            mdDistributor.getOrCreateTs(idx, Interval.Day, 2)
        })
        val intraPrices = Broadcaster<OhlcTO>("intra prices", historyKey = { l -> l.ticker })
        mdDistributor.addListener(Interval.Min10, { time, md ->
            dayTss.forEachIndexed({ idx, ts ->
                val ticker = tickers[idx]
                defferer.executeLater(ticker) {
                    intraPrices.add(convertOhlcToGrpc(ts[0], ticker, OhlcPeriod.Day))
                }
            })
        })
        return intraPrices
    }

    fun makeHistoricalBc(distributor: MarketDataDistributor, tickers: List<String>): Broadcaster<OhlcTO> {
        val broadcaster = Broadcaster<OhlcTO>("prices", maxSize = 600, historyKey = { l -> l.ticker })
        tickers.forEachIndexed({ idx, ticker ->
            distributor.getOrCreateTs(idx, Interval.Day, 2).preRollSubscribe {
                broadcaster.add(convertOhlcToGrpc(it[0], ticker, OhlcPeriod.Day))
            }
        })
        return broadcaster
    }

    fun makeContext(): SimpleRunCtx {
        println("Starting strats")
        val conf = modelConfig(waitOnEnd = true)
        val context = SimpleRunCtx(conf)
        context.addModelWithDefaultParams()
        return context
    }

    fun makeStratBc(context: SimpleRunCtx): Broadcaster<StratDescription> {
        val description = """
# Стратегия "Волабрейк"
## Описание логики 
Является типичной пробойной стратегией ***"long-only"*** (только длинные позиции) с фильтрами которые повышают вероятность прибыльной сделки.
<br/>
Обыкновенно вы увидите оповещение о сигнали с этой стратегии в конце рабочего дня.
<br/>
Среднее удержание сделки **3 дня**
    """.trimIndent()
        val strats = Broadcaster<StratDescription>("stratDescription");
        val tradeStat = TradeStat(stratName = "volBreak", descr = description, strats = strats)
        context.boundModels.first().model.orderManagers().forEach({ om ->
            om.tradesTopic().subscribe { trade ->
                tradeStat.addTrade(trade)
            }
        })
        return strats
    }

}

