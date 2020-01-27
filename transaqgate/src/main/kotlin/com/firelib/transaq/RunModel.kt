package com.firelib.transaq

import com.funstat.tcs.GateEmulator
import firelib.common.core.DummyModel
import firelib.common.core.ProdRunner
import firelib.common.core.SimpleRunCtx
import firelib.common.interval.Interval
import firelib.common.misc.FinamTickerMapper
import java.util.concurrent.Executors


fun main() {

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val stub = makeDefaultStub()

    stub.command(loginCmd)

    val mapper = TrqInstrumentMapper(stub)

    //val gate = makeDefaultTransaqGate(executor)

    val factory = TrqRealtimeReaderFactory(stub, Interval.Sec10, mapper)

    val gate = GateEmulator(executor)

    val finamMapper = FinamTickerMapper()

    try {
        val context = SimpleRunCtx(DummyModel.modelConfig())

        context.gateMapper = mapper

        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory,
            finamMapper
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}