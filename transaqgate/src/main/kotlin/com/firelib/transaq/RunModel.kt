package com.firelib.transaq

import firelib.common.core.DummyModel
import firelib.common.core.ProdRunner
import firelib.common.core.SimpleRunCtx
import java.util.concurrent.Executors


fun main() {

    val executor = Executors.newSingleThreadExecutor { Thread(it,"mainExecutor") }

    //val gate = TcsGate(executor, mapper )

    val stub = makeDefaultStub()

    val gate = makeDefaultTransaqGate(executor)

    val source = TrqSource(stub, "1")

    val symbols = source.symbols().associateBy { it.code }



    try {
        ProdRunner.runStrat(
                executor,
                SimpleRunCtx(DummyModel.modelConfig()),
                gate,
                {symbols[it]!!},
                {symbols[it]!!},
                source
        )

    }catch (e : Exception){
        e.printStackTrace()
    }
}
