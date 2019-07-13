package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.ModelFactory
import firelib.common.interval.Interval
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.ordermanager.flattenAll
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.reader.ReaderFactoryImpl
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import kotlinx.coroutines.coroutineScope
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.abs


class SmaFactory : ModelFactory {
    override fun invoke(context: ModelContext, props: Map<String, String>): Model {
        return SpreadModel(context,props)
    }
}



class SpreadModel(val context: ModelContext, val props: Map<String, String>) : Model{

    val period = props["period"]!!.toInt()

    val ts0 : TimeSeries<Ohlc> = context.mdDistributor.getOrCreateTs(0, Interval.Day, period + 1)
    val ts1 : TimeSeries<Ohlc> = context.mdDistributor.getOrCreateTs(1, Interval.Day, period + 1)

    var count = 0;

    init {
        context.mdDistributor.getOrCreateTs(0,Interval.Min10,2).preRollSubscribe {  }
    }

    override fun update() {
        if(count++ % 1000_000 == 0){
            println("count is ${count}" )
        }

        val r0 = (ts0[0].close - ts0[period].close) / ts0[period].close
        val r1 = (ts1[0].close - ts1[period].close) / ts1[period].close

        val spread = r0 - r1
        if(spread > 0.05){
            omanagers[0].makePositionEqualsTo(-1)
            omanagers[1].makePositionEqualsTo(1)
        }
        if(spread < -0.05){
            omanagers[0].makePositionEqualsTo(1)
            omanagers[1].makePositionEqualsTo(-1)
        }

        if(abs(spread) < 0.005){
            this.omanagers.forEach {it.flattenAll()}
        }

    }

    private var omanagers: List<OrderManagerImpl> = context.instruments
            .map { OrderManagerImpl(context.tradeGate,context.timeService,it)}


    override fun name(): String {
        return "Sma"
    }

    init {
        omanagers[0].tradesTopic().subscribe { println("trade ${it}") }
        omanagers[1].tradesTopic().subscribe { println("trade ${it}") }
    }

    override fun orderManagers(): List<OrderManager> {
        return this.omanagers
    }

    override fun onBacktestEnd() {
        this.omanagers.forEach {it.flattenAll()}
    }

    override fun properties(): Map<String, String> {
        return props
    }
}

suspend fun main() = coroutineScope {
    val conf = ModelBacktestConfig()
    conf.reportTargetPath = "./report"
    conf.startDateGmt = LocalDateTime.now().minusDays(1500).toInstant(ZoneOffset.UTC)
    val factoryImpl = ReaderFactoryImpl("/ddisk/globaldatabase/")


    val storageImpl = MdStorageImpl()


    if(true){
        val divs = DivHelper.getDivs()
        val finamDownloader = FinamDownloader()

        val symbols = finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == "1" }
        symbols.forEach({storageImpl.updateMarketData(it)})
    }

/*
    conf.instruments = listOf(
            InstrumentConfig("aapl",{startTime->



                factoryImpl.create("1MIN/STK/AAPL_1.csv",startTime)
            }),
            InstrumentConfig("goog", {startTime->
                factoryImpl.create("1MIN/STK/GOOG_1.csv", startTime)
            })
    )
    conf.modelParams = mapOf("period" to "10")
    //conf.startDateGmt = LocalDateTime.now().minusDays(600).toInstant(ZoneOffset.UTC)
    conf.precacheMarketData = false
    runSimple(conf, SmaFactory())
    println("some")*/
}