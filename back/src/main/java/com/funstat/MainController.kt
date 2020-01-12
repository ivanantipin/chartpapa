package com.funstat

import com.funstat.domain.Annotations
import com.funstat.domain.InstrId
import com.funstat.domain.StringWrap
import com.funstat.domain.TimePoint
import com.funstat.ohlc.Metadata
import com.funstat.store.CachedStorage
import com.funstat.store.MdStorage
import com.funstat.store.MdStorageImpl
import com.funstat.vantage.VantageDownloader
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.domain.Ohlc
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


object GlobalConstants{
    val mdFolder = Paths.get("/ddisk/globaldatabase/md")
    val rootReportPath =  Paths.get("/home/ivan/projects/chartpapa/market_research/report_out")
}

class MainController {

    internal var storage: MdStorage = CachedStorage(MdStorageImpl(GlobalConstants.mdFolder.toString()))

    internal var allMetas: MutableList<Metadata> = ArrayList()

    val noteBooksDir = Paths.get(System.getProperty("notebooksDir") ?: "/home/ivan/projects/fbackend/market_research/published" )

    init {
        println("notebooks dir is " + noteBooksDir)
    }

    fun loadStaticPages(): List<String> {
        return noteBooksDir.toFile()
                .list { f, name -> name.endsWith("html") }
                .map { it.replace(".html", "") }
    }

    internal fun onStart() {
        storage.updateSymbolsMeta()
        storage.start()
    }

    fun loadAllMetas(): List<Metadata> {
        return allMetas
    }


    fun loadHtmContent(file: String): StringWrap {
        return StringWrap(String(Files.readAllBytes(noteBooksDir.resolve("$file.html"))))
    }


    fun addMetadata(metadata: Metadata) {
        allMetas.add(metadata)
    }

    fun instruments(): Collection<InstrId> {
        return storage.meta().filter { s -> s.source != VantageDownloader.SOURCE }
    }

    fun timeframes(): List<String> {
        return Interval.values().map { it.name }
    }


    fun getOhlcs(instrId: InstrId, interval: String): Collection<Ohlc> {
        return storage.read(instrId, interval)
    }

    fun getAnnotations(instrId: InstrId, interval: String): Annotations {
        val ohlcs = storage.read(instrId, interval)
        return AnnotationCreator.createAnnotations(ohlcs)
    }

    fun getSeries(codes: Array<InstrId>, interval: String): Map<String, Collection<TimePoint>> {
        val mm = HashMap<String, Collection<TimePoint>>()
        Arrays.stream(codes).forEach { c ->
            mm[c.code] = storage.read(c, interval).map { oh -> TimePoint(oh.endTime.atUtc(), oh.close) }.sorted()
        }
        return mm
    }

    fun update(ticker : String){
        storage.updateRequested(ticker)
    }

}