package com.firelib.transaq

import firelib.tcs.GateEmulator
import firelib.tcs.RtReaderEmulator
import firelib.common.core.DummyModel
import firelib.common.core.ProdRunner
import firelib.common.core.ReaderFactory
import firelib.common.core.SimpleRunCtx
import firelib.domain.Interval
import firelib.common.reader.SimplifiedReader
import java.util.concurrent.Executors


fun main() {

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

//    val stub = makeDefaultStub()
//
//    stub.command(loginCmd)

//    val mapper = TrqInstrumentMapper(stub)

    //val factory = TrqRealtimeReaderFactory(stub, Interval.Sec10, mapper)

    val factory = object : ReaderFactory {
        override fun makeReader(security: String): SimplifiedReader {
            return RtReaderEmulator(Interval.Sec10);
        }
    }

    val gate = GateEmulator(executor)

    try {
        val context = SimpleRunCtx(DummyModel.modelConfig())

        context.gateMapper = gate

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