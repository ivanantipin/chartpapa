package firelib.core

import firelib.common.Trade
import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.Ohlc
import firelib.core.domain.OrderState
import firelib.core.mddistributor.MarketDataDistributor
import firelib.core.misc.Batcher
import firelib.core.misc.ChannelSubscription
import firelib.core.misc.StreamTradeCaseGenerator
import firelib.core.report.OhlcStreamWriter
import firelib.core.report.StreamTradeCaseWriter
import firelib.core.report.StreamTradeWriter
import firelib.core.report.dao.ColDefDao
import firelib.core.report.orderColsDefs
import firelib.core.timeseries.nonInterpolatedView
import org.apache.commons.io.FileUtils
import java.nio.file.Path
import java.util.concurrent.ExecutorService


fun enableTradeCasePersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService, tableName : String = "trade_cases") : Persisting{
    FileUtils.forceMkdir(reportFilePath.parent.toFile())

    val tradeCaseWriter = StreamTradeCaseWriter(reportFilePath, tableName)

    val casesBatcher = Batcher<Pair<Trade, Trade>>({
        ioExecutor.submit { tradeCaseWriter.insertTrades(it) }.get()
    }, "cases writer")

    casesBatcher.start()

    val subscriptions = model.orderManagers().map { om ->
        val generator = StreamTradeCaseGenerator()
        om.tradesTopic().subscribe {
            casesBatcher.addAll(generator.genClosedCases(it))
        }
    }

    return Persisting(casesBatcher, subscriptions)
}

class Persisting(val batcher: Batcher<out Any>, val subscriptions: Collection<ChannelSubscription>){
    fun cancelAndJoin(){
        subscriptions.forEach {it.unsubscribe()}
        batcher.cancelAndJoin()
    }
}



fun enableOrdersPersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService, tableName : String = "orders") : Persisting{
    FileUtils.forceMkdir(reportFilePath.parent.toFile())

    val orderWriter = ColDefDao(reportFilePath, orderColsDefs, tableName)
    val orderBatcher = Batcher<OrderState>({
        ioExecutor.submit {
            orderWriter.upsert(it)
        }.get()
    }, "cases writer")
    orderBatcher.start()

    val subscriptions = model.orderManagers().map { om ->
        om.orderStateTopic().subscribe {
            orderBatcher.add(it)
        }
    }


    return Persisting(orderBatcher, subscriptions)
}



fun enableTradeRtPersist(model : Model, reportFilePath : Path, ioExecutor : ExecutorService, tableName : String = "trades") : Persisting{
    FileUtils.forceMkdir(reportFilePath.parent.toFile())

    val tradeWriter = StreamTradeWriter(reportFilePath, tableName)

    val tradesBatcher = Batcher<Trade>({
        ioExecutor.submit { tradeWriter.insertTrades(it)}.get()
    }, "tradesRealtime")

    tradesBatcher.start()

    val subscriptions = model.orderManagers().map { om ->
        om.tradesTopic().subscribe {
            tradesBatcher.add(it)
        }
    }
    return Persisting(tradesBatcher, subscriptions)
}


fun enableOhlcDumping(config: ModelBacktestConfig, marketDataDistributor: MarketDataDistributor, executorService: ExecutorService) : List<Batcher<Ohlc>> {
    return config.instruments.mapIndexed{ instrIdx, instr ->
        val writer = OhlcStreamWriter(config.getReportDbFile())
        val batcher = Batcher<Ohlc>({ executorService.submit({writer.insertOhlcs(instr, it)}).get()  }, instr)

        marketDataDistributor.getOrCreateTs(instrIdx, config.dumpInterval, 2)
                .nonInterpolatedView()
                .preRollSubscribe {
                    batcher.add(it[0].copy(endTime = it[0].endTime.minusMillis(config.dumpInterval.durationMs)))
                }

        batcher.apply {
            start()
        }
    }
}