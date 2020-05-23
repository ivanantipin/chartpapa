package firelib.model

import firelib.core.*
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.config.runStrat
import firelib.core.config.setTradeSize
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.SqlUtils
import firelib.core.misc.atNy
import firelib.core.misc.toInstantMoscow
import firelib.core.misc.toInstantNy
import firelib.core.report.GenericMapWriter
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.MdDaoContainer
import firelib.core.timeseries.TimeSeries
import firelib.core.timeseries.makeUsTimeseries
import firelib.indicators.Ma
import firelib.iqfeed.IqFeedHistoricalSource
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class EdgarUs(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val lst = mutableListOf<Map<String, Any>>()

    init {
        val edgarSymbols = getSymbolsForEdgar()
        val usSeries = enableSeries(Interval.Min30, interpolated = false)

        usSeries.forEachIndexed { idx, ts ->
            val ticker = instruments()[idx]
            val filings = LinkedList(edgarSymbols[ticker]!!.sortedBy { it.time })
            val floatIdx = 20
            ts.preRollSubscribe {

                if (ts.count() > 41 && filings.isNotEmpty()) {

                    val currFiling = filings.first().time

                    if (ts[0].endTime > currFiling.toInstantNy()) {
                        filings.poll()

//                        if(extr(ts, 0, 10) < -0.05){
//                            shortForMoneyIfFlat(idx, 10_000)
//                        }
                        if(extr(ts, 0, 10) > 0.05){
                            shortForMoneyIfFlat(idx, 10_000)
                        }

                        lst += (0..floatIdx).map {
                            mapOf(
                                "idx" to it,
                                "prev20" to extr(ts, floatIdx, 20),
                                "prev10" to extr(ts, floatIdx, 10),
                                "ret" to extr(ts, floatIdx - it, it)
                            )
                        }
                    }
                } else {
                    while (filings.isNotEmpty() && it[0].endTime > filings.first.time.toInstantNy()) {
                        filings.poll()
                    }
                }
            }
        }

        closePosByCondition {
            position(it) != 0 && oms[it].positionTime() < usSeries[it][20].endTime
        }
    }

    private fun extr(ts: TimeSeries<Ohlc>, idx: Int, window: Int): Double {
        return (ts[idx].close - ts[idx + window].close) / ts[idx + window].close
    }


    override fun onBacktestEnd() {
        super.onBacktestEnd()
        GenericMapWriter.write(
            runConfig().getReportDbFile(),
            lst, "EdgarStats"
        )
    }


    companion object {
        fun modelConfig(tradeSize: Int = 10_000): ModelConfig {
            return ModelConfig(EdgarUs::class, ModelBacktestConfig().apply {
                //instruments = MdDaoContainer().getDao(SourceName.IQFEED, Interval.Min30).listAvailableInstruments()
                instruments = getSymbolsForEdgar().keys.toList()
                interval = Interval.Min30
                histSourceName = SourceName.IQFEED
                startDate(LocalDate.now().minusDays(2000))
            }).apply {
                setTradeSize(tradeSize)
            }
        }
    }
}

internal var pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")

data class EdgarFiling(val time: LocalDateTime, val name: String, val form: String)

fun getSymbolsForEdgar(): Map<String, List<EdgarFiling>> {
    val dsForFile = SqlUtils.getDsForFile("/home/ivan/projects/chartpapa/market_research/edgar/data/edgar.db")

    val allNames =
        JdbcTemplate(dsForFile).query("select name,form, accepted from sub_txt where form = '8-K'", { rs, idx ->
            EdgarFiling(
                name = rs.getString("name"),
                form = rs.getString("form"),
                time = LocalDateTime.parse(rs.getString("accepted"), pattern)
            )
        }).groupBy { it.name }

    return IqFeedHistoricalSource().symbols().filter {
        allNames.containsKey(it.name.toUpperCase())
    }.associateBy({ it.code }, { allNames.get(it.name.toUpperCase())!! })
}

fun main() {
    EdgarUs.modelConfig().runStrat()
}