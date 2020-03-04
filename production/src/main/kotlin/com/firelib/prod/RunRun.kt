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
import firelib.model.DummyModel
import firelib.model.TrendModel
import firelib.model.VolatilityBreak
import firelib.model.trendModelConfig
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

    val config = DummyModel.modelConfig()

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



fun main() {
    runNeverRun()
    //runReal()
}

val runLogger = LoggerFactory.getLogger("runRun")

private fun runReal() {

    System.setProperty("env","prod")

    GlobalConstants.ensureDirsExist()

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val config = VolatilityBreak.modelConfig(15_000)

    val stub = makeDefaultStub()

    val mapper = DbMapper(trqMapperWriter())

    config.gateMapper = mapper

    val msgDispatcher = TrqMsgDispatcher(stub)

    enableReconnect(msgDispatcher)

    val gate = TrqGate(msgDispatcher, executor, "na")

    val factory = TrqRealtimeReaderFactory(msgDispatcher, Interval.Min1, mapper)
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