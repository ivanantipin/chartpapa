package com.firelib.prod


import com.firelib.transaq.*
import firelib.core.ProdRunner
import firelib.core.SimpleRunCtx
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import firelib.finam.FinamDownloader
import firelib.model.DummyModel
import java.lang.RuntimeException
import java.util.concurrent.Executors

fun trqMapperWriter(): GeGeWriter<InstrId> {
    return GeGeWriter(
        "trq_instruments",
        GlobalConstants.metaDb,
        InstrId::class,
        listOf("code", "board")
    )
}

fun finamMapperWriter(): GeGeWriter<InstrId> {
    return GeGeWriter(
        "finam_instruments",
        GlobalConstants.metaDb,
        InstrId::class,
        listOf("id", "code", "market")
    )
}


fun populateMapping(writer: GeGeWriter<InstrId>, func : ()->List<InstrId>) : DbMapper {
    val lst = writer.read()
    if (lst.isEmpty()) {
        println("mapping is empty populating")
        val symbols = func()
        writer.write(symbols)
        println("inserted ${symbols.size} instruments")
    }
    return DbMapper(writer, {true})
}


fun main() {

    GlobalConstants.ensureDirsExist()

    val mapper = populateMapping(finamMapperWriter(), {FinamDownloader().symbols()})

    val executor = Executors.newSingleThreadExecutor { Thread(it, "mainExecutor") }

    val config = DummyModel.modelConfig()

    val stub = makeDefaultStub()

    enableReconnect(stub)

    val gate = TrqGate(stub, executor, GlobalConstants.getProp("transaq.client.id"))

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