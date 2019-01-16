package firelib.common.model

import com.funstat.GlobalConstants
import com.funstat.finam.FinamDownloader
import com.funstat.store.MdDao
import com.funstat.store.MdStorageImpl
import com.funstat.store.SqlUtils
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.ModelFactory
import firelib.common.core.runSimple
import firelib.common.interval.Interval
import firelib.common.misc.PositionCloserByTimeOut
import firelib.common.misc.atUtc
import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.reader.MarketDataReaderSql
import firelib.common.reader.ReaderFactoryImpl
import firelib.common.timeseries.TimeSeries
import firelib.domain.Ohlc
import kotlinx.coroutines.coroutineScope
import org.springframework.jdbc.core.JdbcTemplate
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset


data class Div(val ticker : String, val date : LocalDateTime, val div : Double)

class DivFac : ModelFactory{
    override fun invoke(context: ModelContext, props: Map<String, String>): Model {
        return DivModel(context,props)
    }
}

object DivHelper{
    fun getDivs(): Map<String, List<Div>> {
        val dsForFile = SqlUtils.getDsForFile( GlobalConstants.mdFolder.resolve("meta.db").toAbsolutePath().toString())
        val divs = JdbcTemplate(dsForFile).query("select * from dividends",{row,ind->
            Div(row.getString("ticker"), row.getTimestamp("DT").toInstant().atUtc(), row.getDouble("div"))
        })
        return divs.groupBy { it.ticker }
    }
}

class DivModel(val context: ModelContext, val props: Map<String, String>) : Model{
    private var divdivs: List<List<Div>>
    private var indexes: IntArray
    val oman = makeOrderManagers(context)

    init {
        val divMap = DivHelper.getDivs()
        divdivs = context.instruments.map { divMap[it]!! }
        indexes = context.instruments.map { -1 }.toIntArray()

        oman.forEachIndexed({idx,om->
            PositionCloserByTimeOut(om, Duration.ofDays(5),context.mdDistributor,Interval.Min10,idx)
        })
    }

    override fun properties(): Map<String, String> {
        return props
    }

    override fun orderManagers(): List<OrderManager> {
        return oman
    }

    override fun update() {
        val currentTime = context.timeService.currentTime()

        oman.forEachIndexed({idx,om->
            val dd = divdivs[idx]
            val ohlc = context.mdDistributor.price(idx)
            if(indexes[idx] < 0){
                indexes[idx] = dd.indexOfFirst {
                    it.date.toInstant(ZoneOffset.UTC).isAfter(currentTime) }

                if(indexes[idx] < 0){
                    indexes[idx] = dd.size
                    println("not fount in divs ${dd} time ${currentTime.atOffset(ZoneOffset.UTC)}")
                }
            }

            if(indexes[idx] < dd.size && dd[indexes[idx]].date.toInstant(ZoneOffset.UTC).isBefore(currentTime) && !ohlc.interpolated){
                indexes[idx]++
                om.makePositionEqualsTo(100_000/ohlc.close.toInt())
            }
        })
    }
}

suspend fun main() = coroutineScope {
    val conf = ModelBacktestConfig()
    conf.reportTargetPath = "./report"
    //conf.startDateGmt = LocalDateTime.now().minusDays(1500).toInstant(ZoneOffset.UTC)

    val divs = DivHelper.getDivs()

    val storageImpl = MdStorageImpl()

/*

    val finamDownloader = FinamDownloader()
    val symbols = finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == "1" }
    symbols.forEach({storageImpl.updateMarketData(it)})
*/



    //fixme crap
    val mdDao = storageImpl.getDao(FinamDownloader.SOURCE,Interval.Min10.name)

    conf.instruments = divs.keys.map { InstrumentConfig(it, {time->MarketDataReaderSql(mdDao.queryAll(it))}) }

    conf.modelParams = mapOf("holdTimeDays" to "10")
    //conf.startDateGmt = LocalDateTime.now().minusDays(600).toInstant(ZoneOffset.UTC)
    conf.precacheMarketData = false
    runSimple(conf, DivFac())
    println("done")
}