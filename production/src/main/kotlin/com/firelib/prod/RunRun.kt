package com.firelib.prod


import com.firelib.transaq.*
import firelib.core.ProdRunner
import firelib.core.SimpleRunCtx
import firelib.core.domain.Interval
import firelib.core.store.GlobalConstants
import firelib.core.store.trqMapperWriter
import firelib.model.prod.RealDivModel
import firelib.model.prod.TrendModel
import firelib.model.prod.VolatilityBreak
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.reflect.KClass

val runLogger = LoggerFactory.getLogger("runRun")

fun getPosSize(model: KClass<*>): Int {
    return GlobalConstants.getProp("${model.simpleName!!}.trade.size").toInt()
}

val prodModels = mapOf(
    TrendModel::class.simpleName!! to { TrendModel.modelConfig(getPosSize(TrendModel::class)) },
    RealDivModel::class.simpleName!! to { RealDivModel.modelConfig(getPosSize(RealDivModel::class)) },
    VolatilityBreak::class.simpleName!! to { VolatilityBreak.modelConfig(getPosSize(VolatilityBreak::class)) }
)


fun getTrqMicexMapper(): DbMapper {
    return DbMapper(trqMapperWriter(), { it.board == "TQBR" })
}

fun main(args: Array<String>) {
    runReconnect()
    val models = GlobalConstants.getProp("models.to.run").split(",")
    runLogger.info("models to run : ${models}")
    runModels(models)
}


private fun runModels(names: List<String>) {

    val modelConfigs = names.map { prodModels[it]!!() }

    val runConfig = modelConfigs[0].runConfig

    runConfig.maxRiskMoney = GlobalConstants.getProp("max.risk.money").toLong()
    runConfig.maxRiskMoneyPerSec = GlobalConstants.getProp("max.risk.per.sec").toLong()

    GlobalConstants.ensureDirsExist()

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val stub = makeDefaultStub()

    val mapper = getTrqMicexMapper()

    runConfig.gateMapper = mapper

    val msgDispatcher = TrqMsgDispatcher(stub)

    val gate = TrqGate(msgDispatcher, executor)

    val factory = TrqRealtimeReaderFactory(msgDispatcher, Interval.Sec10, mapper)
    try {
        val context = SimpleRunCtx(runConfig)
        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory,
            modelConfigs
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