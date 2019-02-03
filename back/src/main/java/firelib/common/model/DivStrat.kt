package firelib.common.model

import com.funstat.GlobalConstants
import com.funstat.finam.FinamDownloader
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
import firelib.common.reader.MarketDataReaderSql
import firelib.common.report.GenericDumper
import kotlinx.coroutines.coroutineScope
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.file.Paths
import java.time.*


data class Div(val ticker: String, val date: LocalDate, val div: Double)

class DivFac : ModelFactory {
    override fun invoke(context: ModelContext, props: Map<String, String>): Model {
        return DivModel(context, props)
    }
}

object DivHelper {
    fun getDivs(): Map<String, List<Div>> {
        val dsForFile = SqlUtils.getDsForFile(GlobalConstants.mdFolder.resolve("meta.db").toAbsolutePath().toString())
        val divs = JdbcTemplate(dsForFile).query("select * from dividends", { row, ind ->
            Div(row.getString("ticker"), LocalDate.ofEpochDay(row.getLong("DT")), row.getDouble("div"))
        })
        return divs.groupBy { it.ticker }.mapValues { it.value.sortedBy { div -> div.date } }
    }
}


data class Stat(val ticker: String,
                val days: Int,
                val intraPnl: Double,
                val gapPnl: Double,
                val divDate: Instant,
                val curDate: Instant,
                val divSize: Double,
                val lastMonthReturn : Double
                )

class DivModel(val context: ModelContext, val props: Map<String, String>) : Model {
    val oman = makeOrderManagers(context)

    init {
        val divMap = DivHelper.getDivs()
        val divdivs = context.instruments.map { divMap[it]!!.sortedBy { it.date } }
        val nextIdxes = context.instruments.map { -1 }.toIntArray()

        val dumper = GenericDumper<Stat>("divstat", Paths.get(context.config.reportTargetPath).resolve("stat.db"), Stat::class)

        context.instruments.forEachIndexed({ idx, instrument ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Day, 100)

            ret.preRollSubscribe {

                val currentTime = context.timeService.currentTime()

                val divs = divdivs[idx]


                nextIdxes[idx] = divs.indexOfFirst {
                    it.date.atStartOfDay().isAfter(currentTime.atUtc())
                }

                val nextIdx = nextIdxes[idx]


                val ohlc = ret[0]

                if (nextIdx > 0 && nextIdx < divs.size && !ohlc.interpolated) {

                    val div = divs[nextIdx - 1]
                    val divDate = div.date

                    var diff = (ohlc.dtGmtEnd.atUtc().toLocalDate().toEpochDay() - divDate.toEpochDay()).toInt()

                    if (diff == 20) {

                        val ff = (0 until 40).
                                map { ret[it] }
                                .filter { !it.interpolated }
                                .reversed()

                        val divIdx = ff.indexOfLast { divDate.isAfter(it.dtGmtEnd.atUtc().toLocalDate()) } + 1

                        dumper.write(
                                (1 until ff.size).map{idx->
                                    val oh = ff[idx]
                            Stat(instrument, idx - divIdx ,
                                    intraPnl = (oh.close - oh.open)/oh.open,
                                    gapPnl = (oh.open - ff[idx - 1].close) / ff[idx - 1].close,
                                    curDate = oh.dtGmtEnd,
                                    divDate = divDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                    divSize = div.div/oh.open,
                                    lastMonthReturn =  (ret[21].close - ret[51].close)/ret[51].close
                            )
                        })
                    }
                }
            }
            ret
        })
        oman.forEachIndexed({ idx, om ->
            PositionCloserByTimeOut(om, Duration.ofDays(5), context.mdDistributor, Interval.Min10, idx)
        })
    }

    override fun properties(): Map<String, String> {
        return props
    }

    override fun orderManagers(): List<OrderManager> {
        return oman
    }

    override fun update() {}
}

suspend fun main() = coroutineScope {
    //conf.startDateGmt = LocalDateTime.now().minusDays(1500).toInstant(ZoneOffset.UTC)

    val divs = DivHelper.getDivs()


    val storageImpl = MdStorageImpl()

    fun update() {
        val finamDownloader = FinamDownloader()
        val symbols = finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == "1" }
        symbols.forEach({ storageImpl.updateMarketData(it) })

    }


    val conf = ModelBacktestConfig()
    conf.reportTargetPath = "./report"

    val mdDao = storageImpl.getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    conf.instruments = DivHelper.getDivs().keys.map { InstrumentConfig(it, { time -> MarketDataReaderSql(mdDao.queryAll(it)) }) }

    conf.modelParams = mapOf("holdTimeDays" to "10")
    //conf.startDateGmt = LocalDateTime.now().minusDays(600).toInstant(ZoneOffset.UTC)
    conf.precacheMarketData = false
    runSimple(conf, DivFac())
    println("done")

}