package com.firelib.transaq

import firelib.emulator.GateEmulator
import firelib.emulator.RtReaderEmulator
import firelib.model.DummyModel
import firelib.core.ProdRunner
import firelib.core.store.ReaderFactory
import firelib.core.SimpleRunCtx
import firelib.core.domain.Interval
import firelib.core.store.reader.SimplifiedReader
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