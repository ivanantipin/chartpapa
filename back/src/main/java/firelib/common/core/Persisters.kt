package firelib.common.core

import firelib.common.Order
import firelib.common.OrderStatus
import firelib.common.Trade
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.model.Model
import firelib.common.report.dao.OhlcStreamWriter
import firelib.common.report.dao.ColDefDao
import firelib.common.report.dao.StreamTradeCaseWriter
import firelib.common.report.orderColsDefs
import firelib.common.timeseries.nonInterpolatedView
import firelib.domain.Ohlc
import java.nio.file.Path
import java.util.concurrent.ExecutorService

fun enableTradeCasePersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService) : Batcher<Pair<Trade, Trade>>{
    val tradeCaseWriter = StreamTradeCaseWriter(reportFilePath)

    val casesBatcher = Batcher<Pair<Trade, Trade>>({
        ioExecutor.submit({tradeCaseWriter.insertTrades(it)}).get()
    }, "cases writer")

    casesBatcher.start()


    model.orderManagers().forEach({om->
        val generator = StreamTradeCaseGenerator()
        om.tradesTopic().subscribe {
            casesBatcher.addAll(generator.genClosedCases(it))
        }
    })

    return casesBatcher
}

fun enableOrdersPersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService) : Batcher<Order>{
    val orderWriter = ColDefDao(reportFilePath, orderColsDefs, "orders")

    val orderBatcher = Batcher<Order>({

        ioExecutor.submit({
            orderWriter.upsert(it)

        }).get()
    }, "cases writer")

    orderBatcher.start()

    model.orderManagers().forEach({om->
        om.orderStateTopic().subscribe {
            if(it.status == OrderStatus.New){
                orderBatcher.add(it.order)
            }
        }
    })
    return orderBatcher
}



fun enableTradeRtPersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService) : Batcher<Trade>{

    val tradeWriter = StreamTradeCaseWriter( reportFilePath, "singleTrades")

    val tradesBatcher = Batcher<Trade>({
        ioExecutor.submit({tradeWriter.insertTrades(it.map { Pair(it,it) })}).get()}, "tradesRealtime")

    tradesBatcher.start()

    model.orderManagers().forEach({om->
        om.tradesTopic().subscribe {
            tradesBatcher.add(it)
        }
    })
    return tradesBatcher
}


fun enableOhlcDumping(config: ModelBacktestConfig, marketDataDistributor: MarketDataDistributor) : List<Batcher<Ohlc>> {
    return config.instruments.mapIndexed{ instrIdx, instr ->
        val writer = OhlcStreamWriter(config.getReportDbFile())
        val batcher = Batcher<Ohlc>({ writer.insertOhlcs(instr.ticker, it) }, instr.ticker)

        marketDataDistributor.getOrCreateTs(instrIdx, Interval.Min240, 2)
                .nonInterpolatedView()
                .preRollSubscribe({
                    batcher.add(it[0])
                })

        batcher.apply {
            isDaemon = true
            start()
        }
    }
}
