package com.firelib.stratserver

import com.firelib.*
import com.firelib.stratserver.ServiceFac.makeContext
import com.firelib.stratserver.ServiceFac.makeHistoricalBc
import com.firelib.stratserver.ServiceFac.makeInterServiceBc
import com.firelib.stratserver.ServiceFac.makeLevelsBc
import com.firelib.stratserver.ServiceFac.makeStratBc
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.time.Instant

class ServiceImpl() : StratServiceGrpc.StratServiceImplBase() {

    val log = LoggerFactory.getLogger(javaClass)

    val context = makeContext()

    val distributor = context.marketDataDistributor

    val tickers = context.modelConfig.instruments

    val strats = makeStratBc(context)

    val levels = makeLevelsBc(distributor, tickers)

    val historicalPrices = makeHistoricalBc(distributor, tickers)

    val intraPrices = makeInterServiceBc(distributor, tickers)

    override fun getStrats(request: Empty, responseObserver: StreamObserver<StratDescription>) {
        strats.addObserver(responseObserver)
    }

    override fun getLevels(request: Empty, responseObserver: StreamObserver<Levels>) {
        levels.addObserver(responseObserver);
    }

    override fun priceSubscribe(request: HistoryRequest, responseObserver: StreamObserver<OhlcTO>) {
        historicalPrices.addObserver(responseObserver)
    }

    override fun intradaySubscribe(request: Empty, responseObserver: StreamObserver<OhlcTO>) {
        intraPrices.addObserver(responseObserver);
    }

    fun runBlocking(){
        try {
            context.backtest(Instant.MAX)
        }catch (e : Exception){
            log.error("failed", e)
        }
        log.info("done")
    }
}