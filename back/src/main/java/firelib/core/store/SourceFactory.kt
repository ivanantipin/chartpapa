package firelib.core.store

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.emulator.HistoricalSourceEmulator
import firelib.eodhist.EodHistSource
import firelib.finam.FinamDownloader
import firelib.finam.MoexSource
import firelib.iqfeed.IqFeedHistoricalSource
import firelib.poligon.PoligonSource
import firelib.vantage.VantageDownloader
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap


fun interface HistoricalSourceProvider{
    operator fun get(source: SourceName) : HistoricalSource
}

class SourceFactory : HistoricalSourceProvider{
    val sources = mapOf(
        SourceName.FINAM to { FinamDownloader() },
        SourceName.VANTAGE to { VantageDownloader() },
        SourceName.DUMMY to { HistoricalSourceEmulator() },
        SourceName.MOEX to { MoexSource() },
        SourceName.IQFEED to { IqFeedHistoricalSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")) },
        SourceName.EODHIST to { EodHistSource() },
        SourceName.POLIGON to { PoligonSource(GlobalConstants.getProp("POLYGON_TOKEN")) },
    )

    val concurrentHashMap = ConcurrentHashMap<SourceName, HistoricalSource>()

    override operator fun get(source: SourceName) : HistoricalSource {
        return concurrentHashMap.computeIfAbsent(source, {sources[source]!!()})
    }
}