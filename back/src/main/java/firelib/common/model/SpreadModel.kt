package firelib.common.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.common.misc.UtilsHandy
import firelib.core.flattenAll
import firelib.core.makePositionEqualsTo
import firelib.core.timeseries.TimeSeries
import firelib.core.domain.Ohlc
import java.time.LocalDate
import kotlin.math.abs

class SpreadModel(context: ModelContext, val props: Map<String, String>) : Model(context,props){

    val period = props["period"]!!.toInt()

    val ts0 : TimeSeries<Ohlc> = context.mdDistributor.getOrCreateTs(0, Interval.Day, period + 1)
    val ts1 : TimeSeries<Ohlc> = context.mdDistributor.getOrCreateTs(1, Interval.Day, period + 1)

    var count = 0;


    override fun update() {
        val omanagers = orderManagers()
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
            omanagers.forEach {it.flattenAll()}
        }

    }

    init {
        orderManagers()[0].tradesTopic().subscribe { println("trade ${it}") }
        orderManagers()[1].tradesTopic().subscribe { println("trade ${it}") }
    }
}

fun main() {
    val conf = ModelBacktestConfig(SpreadModel::class).apply{
        startDate(LocalDate.now().minusDays(1500))
    }
    UtilsHandy.updateRussianDivStocks()
    conf.runStrat()
}