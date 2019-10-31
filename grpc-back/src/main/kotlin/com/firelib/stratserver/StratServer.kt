package com.firelib.stratserver

import com.firelib.Empty
import com.firelib.StratDescription
import com.firelib.StratServiceGrpc
import firelib.common.interval.Interval
import firelib.common.model.UtilsHandy
import firelib.common.model.VolatilityBreak
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class StratServer {

    private val server: Server

    val service = ServiceImpl()

    init {
        val port = 50051
        server = ServerBuilder.forPort(port)
                .addService(service)
                .build()
                .start()
        println("Server started, listening on ${port}")
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down")
                this@StratServer.stop()
                System.err.println("*** server shut down")
            }
        })

    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    class ServiceImpl : StratServiceGrpc.StratServiceImplBase() {

        val strats = Brodcaster<StratDescription>("stratDescription");

        val tradeStat = TradeStat("volBreak", "empty", strats)

        init {
            strats.start()
        }

        override fun getStrats(request: Empty, responseObserver: StreamObserver<StratDescription>) {
            println("subscribe to strats")
            strats.addObserver(responseObserver)
        }
    }

    suspend fun runStrat() {
        println("Starting strats")
        VolatilityBreak.runDefault(waitOnEnd = true, ctxListener =  {model->
            model.context.mdDistributor.addListener(Interval.Min10,{time,md->
                val priceMap = model.context.instruments.mapIndexed({idx,tick-> Pair(tick,md.price(idx))}).toMap()
                service.tradeStat.updatePrices(priceMap)

            })

            model.orderManagers().forEachIndexed({ idx, om ->

                om.tradesTopic().subscribe { trade ->
                    service.tradeStat.addTrade(trade)
                }
            })
        })
    }
}

suspend fun main() {
    val server = StratServer()

//    UtilsHandy.updateRussianDivStocks()

    GlobalScope.launch {
        server.runStrat();
    }
    GlobalScope.launch {
        while (true){
            println("updating stocks")
            try {
                UtilsHandy.updateRussianDivStocks()
            }catch (e : java.lang.Exception){
                println("error updating stocks ${e}")
            }

            Thread.sleep(300000)
        }
    }
    server.blockUntilShutdown()
}

