package firelib.core.misc

import firelib.core.HistoricalSource
import firelib.core.domain.InstrId
import firelib.finam.FinamDownloader
import firelib.core.store.MdStorageImpl
import firelib.core.InstrumentMapper
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.UtilsHandy.updateTicker
import firelib.core.store.GlobalConstants
import firelib.core.store.finamMapperWriter
import firelib.model.DivHelper
import firelib.vantage.VantageDownloader
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileReader
import java.lang.Double
import java.nio.charset.Charset
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class FinamTickerMapper(val finamDownloader: FinamDownloader) : InstrumentMapper {

    val symbols by lazy {
        finamDownloader.symbols()
    }

    val code2instr by lazy {
        symbols.groupBy { it.code.toLowerCase() }
    }

    override fun invoke(ticker: String): InstrId? {
        val lst = code2instr[ticker.toLowerCase()]!!
        return lst.filter { it.market == FinamDownloader.SHARES_MARKET }.firstOrNull()
    }
}


object UtilsHandy {

    val log = LoggerFactory.getLogger(javaClass)

    fun updateRussianDivStocks(market: String = FinamDownloader.SHARES_MARKET): List<Pair<String, Instant>> {
        log.info("updating tickers that have a divs")
        val divs = DivHelper.getDivs()
        log.info("tickers to update ${divs.keys}")
        val storageImpl = MdStorageImpl()
        val finamDownloader = FinamDownloader()
        val symbols =
            finamDownloader.symbols().filter { divs.containsKey(it.code.toLowerCase()) && it.market == market }
        return symbols.map { Pair(it.code, storageImpl.updateMarketData(it)) }
    }


    fun updateTicker(ticker: String, market: String = FinamDownloader.SHARES_MARKET) {
        val downloader = FinamDownloader()

        val symbols = downloader.symbols()

        val instr = symbols.find { it.code.equals(ticker, true) && it.market == market }

        if (instr != null) {
            log.info("updating instrument ${instr}")
            MdStorageImpl().updateMarketData(instr)
        } else {
            log.info("instrument not found ${ticker}")
        }
        downloader.close()

    }

}

val mt5formate = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

fun parseMt5(str: String): Ohlc? {
    try {
        val arr = str.split(",").toTypedArray()
        return Ohlc(
            LocalDateTime.parse(arr[0], mt5formate).toInstant(ZoneOffset.UTC),
            Double.parseDouble(arr[1]),
            Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), Double.parseDouble(arr[4]), 0, arr[6].toLong(), false)
    } catch (e: Exception) {
        println("not valid entry " + str + " because " + e.message)
        return null
    }
}


class Mt5CsvSource : HistoricalSource{
    override fun symbols(): List<InstrId> {
        return File("/home/ivan/transaq/md/").list().map {
            InstrId(code = it!!)
        }
    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        val reade = FileReader("/home/ivan/transaq/md/${instrId.code}H4.csv", Charset.forName("unicode"))
        return sequence<Ohlc> {
            yieldAll(reade.readLines().map({parseMt5(it)!!}))
        }
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {
        return load(instrId)
    }

    override fun getName(): SourceName {
        return SourceName.MT5
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min240
    }

}


fun main(args: Array<String>) {
    MdStorageImpl().updateMarketData(InstrId(code = "FUTSP500CONT", source = SourceName.MT5.name));
}