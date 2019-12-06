package firelib.common.core

import com.funstat.domain.InstrId
import com.funstat.store.MdStorageImpl
import com.funstat.tcs.TcsGate
import firelib.common.config.ModelBacktestConfig
import firelib.common.tradegate.TradeGate
import firelib.domain.Ohlc
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue


object ProdRunner {

    fun runStrat(executorService: ExecutorService,
                 cfg: ModelBacktestConfig,
                 realGate: TradeGate,
                 backtestMapper: (ticker: String) -> InstrId,
                 realtimeMapper: (ticker: String) -> InstrId,
                 realtimeSource: Source,
                 modelFactory: ModelFactory) {

        val storageImpl = MdStorageImpl()

        val endTime = cfg.instruments.map { it.ticker }.map({
            storageImpl.updateMarketData(backtestMapper(it))
        }).min()!!

        val context = Launcher.makeContext(cfg, modelFactory, {})

        println("end of hist time is ${endTime}")

        val fut = executorService.submit(Callable<Instant> {
            context.backtest(endTime)
        })

        val queues = cfg.instruments.map { realtimeMapper(it.ticker) }.map {
            println("initing realtime poller for  ${it}")
            val queue = LinkedBlockingQueue<Ohlc>()
            realtimeSource.listen(it, { queue += it })
            queue
        }

        context.tradeGate.setActiveReal(realGate)

        var ct = fut.get()
        println("backtest ended starting from ${ct}")

        while (true) {
            waitUntil(ct + Duration.ofMillis(500))
            val ctt = ct;
            executorService.submit({
                try {
                    queues.forEachIndexed { idx, poller ->
                        poller.pollOhlcsTill(ctt).forEach {
                            println("polled ohlc ${it}")
                            context.marketDataDistributor.addOhlc(idx, it)
                        }
                    }
                    context.time(ctt)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            })
            ct += cfg.rootInterval.duration
        }
    }
}


