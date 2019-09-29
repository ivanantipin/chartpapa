/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.helloworld

import com.firelib.*
import firelib.common.model.VolatilityBreak
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

class StratServer {

    private var server: Server? = null

    val service = ServiceImpl()

    fun start() {
        /* The port on which the server should run */
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
        server?.shutdown()
    }

    fun blockUntilShutdown() {
        server?.awaitTermination()
    }

    class ServiceImpl : StratServiceGrpc.StratServiceImplBase() {

        val observers = mutableListOf<StreamObserver<Signal>>()

        override fun getTickers(request: Empty?, responseObserver: StreamObserver<Tickers>?) {
            val tickers = Tickers.newBuilder().addAllTickers(listOf("sber", "tatn")).build()
            responseObserver!!.onNext(tickers);
            responseObserver.onCompleted()
        }

        override fun subscribe(request: Tickers?, responseObserver: StreamObserver<Signal>?) {
            observers += responseObserver!!;
        }



    }

    suspend fun startStrats(){

        VolatilityBreak.runDefault( {
            it.orderManagers().forEach { om ->
                om.tradesTopic().subscribe {trade->
                    service.observers.forEach({obs->
                        val signal = Signal.newBuilder()
                                .setDescription("security ${trade.security()}  ${trade.order.side}").build()
                        obs.onNext(signal)
                    })

                }

            }
        })

    }

}

suspend fun main() {
    val server = StratServer()
    server.start()
    server.startStrats()
    server.blockUntilShutdown()
}

