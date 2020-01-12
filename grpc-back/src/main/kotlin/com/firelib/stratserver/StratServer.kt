package com.firelib.stratserver

import io.grpc.BindableService
import io.grpc.ServerBuilder


fun grpcServerRun(port: Int, services: List<BindableService>) {
    val serverBuilder = ServerBuilder.forPort(port)
    services.forEach { serverBuilder.addService(it) }
    val server = serverBuilder.build().start()
    println("Server started, listening on ${port}")
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down")
            server.shutdown()
            System.err.println("*** server shut down")
        }
    })
    server.awaitTermination()

}


fun main() {

//    UtilsHandy.updateRussianDivStocks()

    val service = ServiceImpl()


    Thread({
        grpcServerRun(50051, listOf(service))
    }, "grpcServerStart").start()


/*
    GlobalScope.launch {
        while (true) {
            println("updating stocks")
            try {
                UtilsHandy.updateRussianDivStocks()
            } catch (e: java.lang.Exception) {
                println("error updating stocks ${e}")
            }

            Thread.sleep(300000)
        }
    }
*/

    Thread({
        service.runBlocking()
    }, "mainStrategyThread").start()

}