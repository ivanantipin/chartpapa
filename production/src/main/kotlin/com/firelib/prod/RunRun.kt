package com.firelib.prod


import com.firelib.transaq.*
import firelib.core.ProdRunner
import firelib.core.SimpleRunCtx
import firelib.core.domain.Interval
import firelib.core.store.GlobalConstants
import firelib.core.store.trqMapperWriter
import firelib.model.DummyModel
import firelib.model.prod.RealDivModel
import firelib.model.prod.ReversModel
import firelib.model.prod.TrendModel
import firelib.model.prod.VolatilityBreak
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

val prodModels = mapOf(
    VolatilityBreak::class.simpleName!! to { VolatilityBreak.modelConfig(15_000) },
    TrendModel::class.simpleName!! to { TrendModel.modelConfig(15_000) },
    RealDivModel::class.simpleName!! to { RealDivModel.modelConfig(30_000) },
    ReversModel::class.simpleName!! to { ReversModel.modelConfig(30_000) },
    "DummyModel" to { DummyModel.modelConfig() }
)


fun main(args: Array<String>) {

    runReal("DummyModel")

/*
    if (args[0] == "reconnect") {
        runReconnect()
    } else {
        runReal(args[0])
    }
*/

}

val runLogger = LoggerFactory.getLogger("runRun")

private fun runReal(name: String) {

    System.setProperty("env", "prod")

    val config = prodModels[name]!!()

    GlobalConstants.ensureDirsExist()

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val stub = makeDefaultStub()

    val mapper = DbMapper(trqMapperWriter(), { it.board == "FUT" })

    config.runConfig.gateMapper = mapper

    val msgDispatcher = TrqMsgDispatcher(stub)

    val gate = TrqGate(msgDispatcher, executor, "T9009h5")

    val factory = TrqRealtimeReaderFactory(msgDispatcher, Interval.Sec10, mapper)
    try {
        val context = SimpleRunCtx(config.runConfig)
        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory,
            listOf(config)
        )
    } catch (e: Exception) {
        runLogger.error("failed to start strategy", e)
    }
}

fun runReconnect() {
    val stub = makeDefaultStub()

    val msgDispatcher = TrqMsgDispatcher(stub)

    enableReconnect(msgDispatcher)

    val dispatcher = TrqMsgDispatcher(stub)

    dispatcher.addSync<TrqClient>({ it is TrqClient }, {
        clientsDao.write(
            listOf(
                TrqClientDb(
                    id = it.id!!,
                    market = it.market!!,
                    currency = it.currency!!,
                    type = it.type!!
                )
            )
        )
        runLogger.info("written client ${it}")
    })

}