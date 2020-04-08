package com.firelib.prod


import com.firelib.transaq.*
import firelib.core.InstrumentMapper
import firelib.core.ProdRunner
import firelib.core.SimpleRunCtx
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.store.*
import firelib.core.store.reader.SimplifiedReader
import firelib.emulator.GateEmulator
import firelib.emulator.HistoricalSourceEmulator
import firelib.emulator.RtReaderEmulator
import firelib.model.*
import firelib.model.prod.RealDivModel
import firelib.model.prod.ReverseModel
import firelib.model.prod.TrendModel
import firelib.model.prod.VolatilityBreak
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors


fun runDummy() {
    GlobalConstants.ensureDirsExist()

    val historicalSourceEmulator = HistoricalSourceEmulator(Interval.Min1)

    val mapper = populateMapping(
        dummyMapperWriter(),
        { historicalSourceEmulator.symbols() })

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val config = DummyModel.modelConfig()

    config.backtestHistSource = historicalSourceEmulator
    config.gateMapper = mapper

    val gate = GateEmulator(executor)

    val factory = object : ReaderFactory {
        override fun makeReader(security: String): SimplifiedReader {
            return RtReaderEmulator(Interval.Min1)
        }
    }

    try {

        val context = SimpleRunCtx(config)

        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun runNeverRun() {
    System.setProperty("env","prod")
    GlobalConstants.ensureDirsExist()

    val historicalSourceEmulator = HistoricalSourceEmulator(Interval.Sec10)

    val mapper = object: InstrumentMapper{
        override fun invoke(p1: String): InstrId? {
            return InstrId(code = p1)
        }
    }
    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val config = NeverTradeModel.modelConfig()

    config.backtestHistSource = historicalSourceEmulator
    config.gateMapper = mapper

    val gate = GateEmulator(executor)

    val factory = TrqRealtimeReaderFactory(TrqMsgDispatcher(makeDefaultStub()), Interval.Sec10, DbMapper(trqMapperWriter()))

    try {

        val context = SimpleRunCtx(config)

        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

val prodModels = mapOf(
    VolatilityBreak::class.simpleName!! to { VolatilityBreak.modelConfig(15_000)},
    TrendModel::class.simpleName!! to { TrendModel.modelConfig(15_000) },
    RealDivModel::class.simpleName!! to { RealDivModel.modelConfig(30_000)},
    ReverseModel::class.simpleName!! to { ReverseModel.modelConfig(30_000)}
)



fun main(args: Array<String>) {
    runReal(args[0])
}

val runLogger = LoggerFactory.getLogger("runRun")

private fun runReal(name : String) {

    System.setProperty("env","prod")

    val config = prodModels[name]!!()

    GlobalConstants.ensureDirsExist()

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val stub = makeDefaultStub()

    val mapper = DbMapper(trqMapperWriter(), {it.board == "TQBR"})

    config.gateMapper = mapper

    val msgDispatcher = TrqMsgDispatcher(stub)

    enableReconnect(msgDispatcher)

    val gate = TrqGate(msgDispatcher, executor, "T9009h5")

    val factory = TrqRealtimeReaderFactory(msgDispatcher, Interval.Sec10, mapper)
    try {
        val context = SimpleRunCtx(config)
        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory
        )
    } catch (e: Exception) {
        runLogger.error("failed to start strategy", e)
    }
}