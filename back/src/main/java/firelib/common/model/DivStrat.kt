package firelib.common.model

import com.funstat.GlobalConstants
import com.funstat.finam.FinamDownloader
import com.funstat.store.MdStorageImpl
import com.funstat.store.SqlUtils
import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.config.runStrat
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.common.reader.MarketDataReaderSql
import firelib.common.report.GenericDumper
import org.springframework.jdbc.core.JdbcTemplate
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset


data class Div(val ticker: String, val lastDayWithDivs: LocalDate, val div: Double)

object DivHelper {

    val tSwitch = LocalDate.of(2013, Month.SEPTEMBER, 2)

    fun getDivs(): Map<String, List<Div>> {

        val storage = MdStorageImpl()

        val dao = storage.getDao(FinamDownloader.SOURCE, Interval.Min10.toString())

        var trdDays = dao.queryAll("sber").map { it.dtGmtEnd.atUtc().toLocalDate()!! }.toSet()

        if(trdDays.isEmpty()){
            updateRussianStockSimple("sber")
            trdDays = dao.queryAll("sber").map { it.dtGmtEnd.atUtc().toLocalDate()!! }.toSet()
        }

        val dsForFile = SqlUtils.getDsForFile(GlobalConstants.mdFolder.resolve("meta.db").toAbsolutePath().toString())
        val divs = JdbcTemplate(dsForFile).query("select * from dividends", { row, _ ->
            var divDate = LocalDate.ofEpochDay(row.getLong("DT"))!!
            var cntDif = if (divDate.isAfter(tSwitch)) 2 else 0;
            while (cntDif > 0) {
                divDate = divDate.minusDays(1)
                if (trdDays.contains(divDate)) {
                    cntDif--
                }
            }


            Div(row.getString("ticker"), divDate, row.getDouble("div"))
        })
        return divs.groupBy { it.ticker }.mapValues { it.value.sortedBy { div -> div.lastDayWithDivs } }
    }
}


data class Stat(val ticker: String,
                val days: Int,
                val intraPnl: Double,
                val gapPnl: Double,
                val divDate: Instant,
                val curDate: Instant,
                val divSize: Double,
                val lastMonthReturn: Double
)

class DivModel( context: ModelContext,  props: Map<String, String>) : Model(context, props) {

    init {
        val divMap = DivHelper.getDivs()
        val divdivs = context.instruments.map { divMap[it]!!.sortedBy { it.lastDayWithDivs } }
        val nextIdxes = context.instruments.map { -1 }.toIntArray()

        val dumper = GenericDumper<Stat>("divstat", Paths.get(context.config.reportTargetPath).resolve("stat.db"), Stat::class)

        context.instruments.forEachIndexed({ idx, instrument ->
            val ret = context.mdDistributor.getOrCreateTs(idx, Interval.Day, 100)

            ret.preRollSubscribe {

                val currentTime = context.timeService.currentTime()

                val divs = divdivs[idx]


                nextIdxes[idx] = divs.indexOfFirst {
                    it.lastDayWithDivs.atStartOfDay().isAfter(currentTime.atUtc())
                }

                val nextIdx = nextIdxes[idx]


                val ohlc = ret[0]

                if (nextIdx > 0 && nextIdx < divs.size && !ohlc.interpolated) {

                    val div = divs[nextIdx - 1]
                    val divDate = div.lastDayWithDivs

                    var diff = (ohlc.dtGmtEnd.atUtc().toLocalDate().toEpochDay() - divDate.toEpochDay()).toInt()

                    if (diff == 20) {

                        val ff = (0 until 40).map { ret[it] }
                                .filter { !it.interpolated }
                                .reversed()

                        val divIdx = ff.indexOfLast { it.dtGmtEnd.atUtc().toLocalDate() == divDate }

                        dumper.write(
                                (1 until ff.size).map { idx ->
                                    val oh = ff[idx]
                                    Stat(instrument, idx - divIdx,
                                            intraPnl = (oh.close - oh.open) / oh.open,
                                            gapPnl = (oh.open - ff[idx - 1].close) / ff[idx - 1].close,
                                            curDate = oh.dtGmtEnd,
                                            divDate = divDate.atStartOfDay().toInstant(ZoneOffset.UTC),
                                            divSize = div.div / oh.open,
                                            lastMonthReturn = (ret[21].close - ret[51].close) / ret[51].close
                                    )
                                })
                    }
                }
            }
            ret
        })
        closePositionByTimeout(days = 5)
    }
}

suspend fun main() {

    val mdDao = MdStorageImpl().getDao(FinamDownloader.SOURCE, Interval.Min10.name)

    val conf = ModelBacktestConfig().apply {
        reportTargetPath = "./report/divStrat0"
        instruments = DivHelper.getDivs().keys.map { InstrumentConfig(it, { time -> MarketDataReaderSql(mdDao.queryAll(it)) }) }
        param("holdTimeDays", 10)
    }
    conf.runStrat({ctx, prop->DivModel(ctx,prop)})
}