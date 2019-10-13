package com.firelib.stratserver

import com.firelib.*
import firelib.common.Trade
import firelib.common.interval.Interval
import firelib.common.misc.StreamTradeCaseGenerator
import firelib.common.misc.pnl
import firelib.common.model.VolatilityBreak
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class StratServer {

    private val server: Server

    val service = ServiceImpl()

    init {
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
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    class ServiceImpl : StratServiceGrpc.StratServiceImplBase() {

        val tradeBroadcaster = Brodcaster<Signal>("trade");
        val positionBroadcaster = Brodcaster<Positions>("postion");
        val positionHistoryBroadcaster = Brodcaster<Position>("posHistory");

        init {
            tradeBroadcaster.start()
            positionBroadcaster.start()
            positionHistoryBroadcaster.start()
        }

        override fun getTickers(request: Empty, responseObserver: StreamObserver<Tickers>) {
            println("getting tickers")
            val tickers = Tickers.newBuilder().addAllTickers(listOf("sber", "tatn")).build()
            responseObserver.onNext(tickers);
            responseObserver.onCompleted()
        }

        override fun positionSubscribe(request: Empty, responseObserver: StreamObserver<Positions>) {
            println("subscribing to positions")
            positionBroadcaster.addObserver(responseObserver);
        }

        override fun posHistorySubscribe(request: Empty, responseObserver: StreamObserver<Position>) {
            println("subscribing to positions histories")
            positionHistoryBroadcaster.addObserver(responseObserver);

        }



        override fun subscribe(request: Tickers, responseObserver: StreamObserver<Signal>) {
            println("subscribing to ${request}")
            tradeBroadcaster.addObserver(responseObserver)
        }

        class Brodcaster<T>(p0: String, val maxSize : Int = 30 ) : Thread(p0) {
            val observersQueue = LinkedBlockingQueue<StreamObserver<T>>()
            val queue = LinkedBlockingQueue<T>()

            fun add(t : T){
                queue += t
            }

            fun addObserver(t : StreamObserver<T>){
                observersQueue += t
            }


            override fun run(){
                val history = LinkedList<T>()
                val observers = mutableListOf<StreamObserver<T>>()
                while (true) {
                    try {
                        val poll = queue.poll(1, TimeUnit.SECONDS)
                        if (poll != null) {
                            history += poll
                            if(history.size > maxSize){
                                history.removeFirst()
                            }
                            val removed = mutableListOf<StreamObserver<T>>();
                            observers.forEach {
                                try {
                                    it.onNext(poll)
                                } catch (e: StatusRuntimeException) {
                                    print("status runtime ${e} happend on ${it} removing")
                                    removed += it;
                                }
                            }
                            observers -= removed
                        }
                        val obs = observersQueue.poll(1,TimeUnit.MILLISECONDS)
                        if(obs != null){
                            history.forEach({
                                obs.onNext(it)
                            })
                            observers += obs
                        }
                    }catch (e : Exception){
                        print("unexpected exception ${e}")
                    }
                }
            }
        }
    }


    suspend fun runStrat() {
        println("Starting strats")
        VolatilityBreak.runDefault{model->

            val poses = mutableMapOf<Int,List<Trade>>();

            model.orderManagers().forEachIndexed({idx, om ->
                val gen = StreamTradeCaseGenerator()

                om.tradesTopic().subscribe { trade ->
                    val closedPoses = gen.addTrade(trade)

                    closedPoses.map {
                        Position.newBuilder()
                                .setTicker(om.security())
                                .setTimestamp(it.first.dtGmt.toEpochMilli())
                                .setPosition(it.first.qty.toLong())
                                .setOpenPrice(it.first.price)
                                .setPnl(it.pnl())
                                .build()
                    }.forEach({
                        service.positionHistoryBroadcaster.add(it)
                    })

                    poses.put(idx,gen.getPosition())

                    service.tradeBroadcaster.add(Signal.newBuilder()
                            .setTicker(trade.security())
                            .setBuySell(if (trade.side().sign > 0) BuySell.Buy else BuySell.Sell)
                            .setTimestamp(trade.dtGmt.toEpochMilli())
                            .setDescription("security ${trade.security()}  ${trade.order.side}").build())
                }
            })

            model.context.mdDistributor.addListener(Interval.Min60, {time,md->

                val positions = Positions.newBuilder().addAllPoses(
                        poses.filter { it.value.isNotEmpty() }.map { (idx,value)->
                            val trade = value.first()
                            val oh = md.price(idx)
                            Position.newBuilder()
                                    .setTicker(trade.security())
                                    .setTimestamp(trade.dtGmt.toEpochMilli())
                                    .setPosition(trade.qty.toLong())
                                    .setOpenPrice(trade.price)
                                    .setPnl(trade.pnl(oh.close))
                                    .build()
                        }
                ).build()
                service.positionBroadcaster.add(positions);
            })

        }
    }
}

suspend fun main() {
    val server = StratServer()

    GlobalScope.launch {
        server.runStrat();
    }
//    GlobalScope.launch {
//        println("updating stocks")
//        UtilsHandy.updateRussianDivStocks()
//        Thread.sleep(10000)
//    }


    server.blockUntilShutdown()
}

