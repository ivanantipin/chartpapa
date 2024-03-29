package firelib.core

import firelib.common.Order
import firelib.common.Trade
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.domain.*
import firelib.core.misc.TelegramMsg
import firelib.core.misc.timeSequence
import firelib.core.domain.ModelNameTicker
import firelib.core.domain.OmPosition
import firelib.core.report.ReportWriter
import firelib.core.report.SqlQueries.readCurrentPositions
import firelib.core.store.GlobalConstants
import firelib.core.store.MdStorage
import firelib.core.store.MdStorageImpl
import firelib.core.store.ReaderFactory
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


object ProdRunner {

    fun runStrat(
        executorService: ExecutorService,
        context: SimpleRunCtx,
        realGate: TradeGate,
        realReaderFactory: ReaderFactory,
        modelConfigs: List<ModelConfig>

    ) {

        val log = LoggerFactory.getLogger(javaClass)

        val cfg = context.runConfig

        val models = modelConfigs.map { context.addModel(it.modelParams, it) }

        val ioExecutor = Executors.newSingleThreadExecutor()

        val nextTimeToProgress = cfg.interval.roundTime(Instant.now())

        if (!cfg.disableBacktest) {
            log.info("end of hist time is ${updateMd(cfg, false)}")

            val persisting = models.flatMap { model ->
                listOf(
                    enableOrdersPersist(model, cfg.getReportDbFile(), ioExecutor),
                    enableTradeCasePersist(model, cfg.getReportDbFile(), ioExecutor)
                )
            }

            val fut = executorService.submit(Callable {
                context.backtest(cfg.interval.roundTime(Instant.now()))
            })


            log.info("backtest ended starting from ${fut.get()}")

            executorService.submit {
                models.flatMap { it.orderManagers() }.forEach { it.flattenAll("switching gate") }
            }.get()

            persisting.forEach { it.cancelAndJoin() }

            ioExecutor.submit {
                ReportWriter.clearReportDir(cfg.reportTargetPath)
                ReportWriter.writeReport(context.boundModels.first(), cfg)
            }.get()

        }

        val curentPoses = readCurrentPositions(GlobalConstants.metaDb)

        curentPoses.forEach {
            context.riskTradeGate.incPos(it.key.ticker, it.value.position)
        }

        executorService.submit {
            models.forEach { model ->
                model.orderManagers().forEach {
                    val key = ModelNameTicker(it.modelName(), it.security().toLowerCase())
                    val pos = curentPoses.getOrDefault(key, OmPosition(0, 0))
                    if (pos.position != 0) {
                        log.info("restored position for model security ${key} to $pos")
                    }
                    it.updatePosition(pos.position, Instant.ofEpochMilli(pos.posTime))
                }
            }
        }.get()

        context.tradeGate.setActiveReal(realGate)

        context.marketDataDistributor.getOrCreateTs(0, Interval.Min240, 10).preRollSubscribe {
            TelegramMsg.sendMsg("alive")
        }

        val realReaders = cfg.instruments.map {
            realReaderFactory.makeReader(it)
        }

        models.forEach { model ->
            enableOrdersPersist(model, GlobalConstants.metaDb, ioExecutor)
            enableTradeCasePersist(model, GlobalConstants.metaDb, ioExecutor)
            enableTradeRtPersist(model, GlobalConstants.metaDb, ioExecutor)
        }

        models.forEach {
            it.oms.forEach({
                it.tradesTopic().subscribe {
                    sendTradeMsg(it)
                }
                it.orderStateTopic().subscribe {
                    sendOrderMsg(it)
                }
            })
        }

        timeSequence(nextTimeToProgress, cfg.interval).forEach {
            try {
                executorService.submit {
                    context.progress(it, realReaders)
                }.get()
            } catch (e: Exception) {
                TelegramMsg.sendMsg("error in the loop ${e.message}")
                log.error("error iterating loop for timestamp $it", e)
            }

        }
    }

    fun sendTradeMsg(it: Trade) {
        TelegramMsg.sendMsg(
            """
        ${it.security()} ${it.side()} 
        modelName=${it.order.modelName} 
        price=${it.price} 
        qty=${it.qty * it.order.instr.lot}
        currentPosition=${it.positionAfter * it.order.instr.lot}
    """.trimIndent()
        )
    }

    fun sendOrderMsg(it: OrderState) {
        TelegramMsg.sendMsg(
            """ Order:
        ${it.order.security} ${it.order.side}
         status=${it.status}
         msg=${it.msg}
        modelName=${it.order.modelName}  
        qty=${it.order.qtyLots * it.order.instr.lot} 
    """.trimIndent()
        )
    }

    val storageImpl = MdStorageImpl()

    fun updateMd(cfg: ModelBacktestConfig, useMin: Boolean): Instant {
        val source = storageImpl.sources[cfg.histSourceName]
        val updated = cfg.instruments.map(source::mapSecurity).associateBy({},
            { storageImpl.updateMarketData(it.copy(source = source.getName().name), cfg.interval) })

        println("updated data to $updated")
        return if (useMin) updated.values.minOrNull()!! else updated.values.maxOrNull()!!
    }
}

fun main() {
    System.setProperty("env", "prod")
    val trd = Trade(
        1,
        10.0,
        Order(
            OrderType.Market,
            1.0,
            10,
            Side.Buy,
            "SBER",
            "id",
            Instant.now(),
            InstrId.dummyInstrument("SBER"),
            "modelName"
        ),
        Instant.now(),
        Instant.now()
    )
    ProdRunner.sendTradeMsg(trd)
}

