package com.firelib.stratserver

import com.firelib.*
import firelib.common.interval.Interval
import firelib.common.model.UtilsHandy
import firelib.common.model.VolatilityBreak
import firelib.common.model.enableSeries
import firelib.domain.Ohlc
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

        val strats = Broadcaster<StratDescription>("stratDescription");

        val levels = Broadcaster<Levels>("levels", historyKey = {l->l.ticker})

        val historicalPrices = Broadcaster<OhlcTO>("prices", maxSize = 600, historyKey = { l->l.ticker})

        val intraPrices = Broadcaster<OhlcTO>("intra prices", historyKey = {l->l.ticker})

        val tradeStat = TradeStat("volBreak", "empty", strats)

        init {
            strats.start()
            levels.start()
        }

        override fun getStrats(request: Empty, responseObserver: StreamObserver<StratDescription>) {
            strats.addObserver(responseObserver)
        }

        override fun getLevels(request: Empty, responseObserver: StreamObserver<Levels>) {
            levels.addObserver(responseObserver);
        }

        override fun priceSubscribe(request: HistoryRequest, responseObserver: StreamObserver<OhlcTO>) {
            historicalPrices.addObserver(responseObserver)
        }

        override fun intradaySubscribe(request: Empty?, responseObserver: StreamObserver<OhlcTO>) {
            intraPrices.addObserver(responseObserver);
        }

    }

    fun convertOhlc(ohlc: Ohlc, tkr : String, op: OhlcPeriod) : OhlcTO{
        return OhlcTO.newBuilder().apply {
            open = ohlc.open
            high = ohlc.high
            low = ohlc.low
            close = ohlc.close
            timestamp = ohlc.dateTimeMs
            ticker = tkr
            period = op

        }.build()
    }

    suspend fun runStrat() {
        println("Starting strats")
        val defferer = Defferer()
        VolatilityBreak.runDefault(waitOnEnd = true, ctxListener =  {model->
            val dayTss = model.enableSeries(Interval.Day, interpolated = false)
            val weekTss = model.enableSeries(Interval.Week, interpolated = false)
            model.context.instruments.forEachIndexed({idx,ticker->
                val levelsGen = LevelsGen(service.levels,ticker)
                dayTss[idx].preRollSubscribe {
                    levelsGen.onOhlc(it[0], Interval.Day)
                    service.historicalPrices.add(convertOhlc(it[0], ticker, OhlcPeriod.Day))

                }
                weekTss[idx].preRollSubscribe {
                    levelsGen.onOhlc(it[0], Interval.Week)
                }
            })

            model.context.mdDistributor.addListener(Interval.Min10,{time,md->
                dayTss.forEachIndexed({idx,ts->
                    val ticker = model.context.instruments[idx]
                    defferer.executeLater(ticker) {
                        service.intraPrices.add(convertOhlc(ts[0], ticker, OhlcPeriod.Day))
                    }
                })
            })

            model.orderManagers().forEach({om ->
                om.tradesTopic().subscribe { trade ->
                    service.tradeStat.addTrade(trade)
                }
            })
        })
    }
}

suspend fun main() {
    val server = StratServer()

    UtilsHandy.updateRussianDivStocks()

    GlobalScope.launch {
        server.runStrat();
    }
    GlobalScope.launch {
        while (true){
            println("updating stocks")
            try {
//                UtilsHandy.updateRussianDivStocks()
            }catch (e : java.lang.Exception){
                println("error updating stocks ${e}")
            }

            Thread.sleep(300000)
        }
    }
    server.blockUntilShutdown()
}
