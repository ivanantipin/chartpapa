package com.firelib.transaq

import firelib.core.ProdRunner
import firelib.core.SimpleRunCtx
import firelib.core.domain.Interval
import firelib.core.store.ReaderFactory
import firelib.core.store.reader.SimplifiedReader
import firelib.emulator.GateEmulator
import firelib.emulator.RtReaderEmulator
import firelib.model.DummyModel
import firelib.model.prod.commonRunConfig
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
        val context = SimpleRunCtx(commonRunConfig())

        ProdRunner.runStrat(
            executor,
            context,
            gate,
            factory, listOf(DummyModel.modelConfig())
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}