package com.firelib.prod


import com.firelib.transaq.*
import firelib.core.ProdRunner
import firelib.core.SimpleRunCtx
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import firelib.model.DummyModel
import java.util.concurrent.Executors

val writer = GeGeWriter(
    "trq_instruments",
    GlobalConstants.metaDb,
    InstrId::class,
    listOf("code", "board")
)

val finamWriter = GeGeWriter(
    "finam_instruments",
    GlobalConstants.metaDb,
    InstrId::class,
    listOf("id", "code", "market")
)

val mapper = DbMapper(GlobalConstants.metaDb, "trq_instruments")

fun main() {


    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }





   val config = DummyModel.modelConfig()

    val stub = makeDefaultStub()

    enableReconnect(stub)

    val gate = TrqGate(stub, executor, "virt/9952")

    Thread.sleep(1000)

    val factory = TrqRealtimeReaderFactory(stub, Interval.Min1, mapper)
    try {

        val context = SimpleRunCtx(config)

        context.gateMapper = mapper

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