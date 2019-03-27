package firelib.common.model

import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import firelib.common.config.InstrumentConfig
import firelib.common.config.ManualOptResourceStrategy
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.OptimizationConfig
import firelib.common.core.runOptimized
import firelib.common.core.runSimple
import firelib.common.interval.Interval
import firelib.common.opt.OptimizedParameter
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.flattenAll
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderDivAdjusted

class TrendModel(val context : ModelContext, val props : Map<String,String>) : Model {

    val oms = makeOrderManagers(context)


    val tss =
        context.instruments.mapIndexed {idx,tickr->
            context.mdDistributor.getOrCreateTs(idx,Interval.Day,100)
        }


    init {
        context.mdDistributor.addListener(Interval.Day,{time,dist->
            if(tss[0].count() > 20 && (tss[0].count() % props["period"]!!.toInt()) == 0){

                //println("update time is ${context.timeService.currentTime()}" )

                val indexed = tss.mapIndexed({ idx, ts -> Pair(idx, (ts[0].close - ts[9].close)/ts[9].close) })

                val sorted = indexed.sortedBy { -it.second }



                sorted.forEachIndexed({idx,pair->
                    if(idx < 3){
                        val orderManager = oms[pair.first]
                        if(orderManager.position() <= 0){
                            orderManager.makePositionEqualsTo((1000000/tss[pair.first][0].close).toInt())
                        }
                    }else if(idx < sorted.size - 3){
                        oms[pair.first].flattenAll()
                    }else{
                        val orderManager = oms[pair.first]
                        if(orderManager.position() >= 0){
                            orderManager.makePositionEqualsTo((-1000000/tss[pair.first][0].close).toInt())
                        }
                    }

                })
            }
        })
    }

    override fun orderManagers(): List<OrderManager> {
        return oms
    }

    override fun update() {



    }

    override fun properties(): Map<String, String> {
        return props
    }
}

suspend fun main(args: Array<String>) {

    val divsMap = DivHelper.getDivs()

    val divs = divsMap

    val conf = ModelBacktestConfig()
    conf.reportTargetPath = "./report/trendModel"

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    conf.instruments = divsMap.keys.toList().subList(0,5).map { instr ->
        InstrumentConfig(instr, { time ->
            ReaderDivAdjusted(MarketDataReaderSql(mdDao.queryAll(instr)), divs[instr]!!)
        })
    }



    conf.precacheMarketData = false

    conf.optConfig.params += OptimizedParameter("period", 7,30,7)
    conf.optConfig.resourceStrategy = ManualOptResourceStrategy(1,100)


    runOptimized(conf, {cfg,fac->
        TrendModel(cfg,fac)
    })
    println("done")

}
