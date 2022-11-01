package firelib.finam

import com.opencsv.CSVParserBuilder
import firelib.core.HistoricalSource
import firelib.core.HistoricalSourceAsync
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.FinamTickerMapper
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FinamDownloader(val batchDays : Int = 100) : AutoCloseable, HistoricalSource {

    val delegate = FinamDownloaderAsync(batchDays)


    override fun close() {
        try {
            delegate.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override fun symbols(): List<InstrId> {
        return runBlocking { delegate.symbols() }
    }


    override fun load(instrIdSpec: InstrId, interval: Interval): Sequence<Ohlc> {
        return load(instrIdSpec, LocalDateTime.now().minusDays(3000), interval)
    }

    @Synchronized
    override fun load(instrIdIn: InstrId, start: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        val instrId =  runBlocking {  delegate.fixInstr(instrIdIn)}
        var mstart = start
        return sequence {
            while (mstart < LocalDateTime.now()) {
                val finish = mstart.plusDays(batchDays.toLong())
                yieldAll(loadSome(instrId, interval, mstart, finish))
                mstart = finish.minus(interval.duration)
            }
        }
    }

    @Synchronized
    private fun loadSome(
        instrId: InstrId,
        interval: Interval,
        start: LocalDateTime,
        finishI: LocalDateTime
    ): List<Ohlc> {
        return runBlocking { delegate.loadSome(instrId, interval, start, finishI) }
    }

    override fun getName(): SourceName {
        return SourceName.FINAM
    }

    enum class FinamMarket(val id: String) {
        SHARES_MARKET("1"),
        FUTURES_MARKET("14"),
        FX("5"),
        BATS("25");
    }

    companion object {
        private val log = LoggerFactory.getLogger(FinamDownloader::class.java)
        val SHARES_MARKET = "1"
        val BATS_MARKET = "25"
        val FX = "5"
    }

    val parser = CSVParserBuilder().withQuoteChar('\'').build()

    internal var pattern = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss")

    val tickerMapper = FinamTickerMapper(this)

    override fun mapSecurity(security: String): InstrId {
        val mappeer = tickerMapper(security)
        require(mappeer != null, {"cant find symbol for ${security}"})
        return mappeer
    }

    override fun getAsyncInterface(): HistoricalSourceAsync? {
        return delegate
    }
}


fun main() {
    FinamDownloader().symbols().forEach { println(it) }
}

