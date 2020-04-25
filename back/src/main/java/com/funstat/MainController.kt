package com.funstat

import com.funstat.domain.Annotations
import com.funstat.domain.StringWrap
import com.funstat.domain.TimePoint
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.domain.sourceEnum
import firelib.core.misc.atUtc
import firelib.core.store.*
import firelib.vantage.VantageDownloader
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*


class MainController {


    val log = LoggerFactory.getLogger(javaClass)

    internal var storage: MdStorage = MdStorageImpl(GlobalConstants.mdFolder.toString())

    internal var allMetas: MutableList<Metadata> = ArrayList()

    val noteBooksDir = Paths.get(System.getProperty("notebooksDir") ?: "/home/ivan/projects/fbackend/market_research/published" )

    init {
        log.info("notebooks dir is " + noteBooksDir)
    }

    fun loadStaticPages(): List<String> {
        return noteBooksDir.toFile()
                .list { f, name -> name.endsWith("html") }
                .map { it.replace(".html", "") }
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
        return storage.meta().filter { s -> s.sourceEnum() != VantageDownloader.SOURCE }
    }

    fun timeframes(): List<String> {
        return Interval.values().map { it.name }
    }


    fun getOhlcs(instrId: InstrId, interval: Interval): Collection<Ohlc> {
        return storage.read(instrId, interval, interval)
    }

    fun getAnnotations(instrId: InstrId, interval: Interval): Annotations {
        val ohlcs = storage.read(instrId, interval, interval)
        return AnnotationCreator.createAnnotations(ohlcs)
    }

    fun getSeries(codes: Array<InstrId>, interval: Interval): Map<String, Collection<TimePoint>> {
        val mm = HashMap<String, Collection<TimePoint>>()
        Arrays.stream(codes).forEach { c ->
            mm[c.code] = storage.read(c, interval, interval).map { oh -> TimePoint(oh.endTime.atUtc(), oh.close) }.sorted()
        }
        return mm
    }

}