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
import firelib.domain.Ohlc
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.*
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct
import javax.validation.Valid


@RestController
@CrossOrigin(origins = ["*"])
class MainController {

    internal var storage: MdStorage = CachedStorage(MdStorageImpl("/ddisk/globaldatabase/md"))

    internal var allMetas: MutableList<Metadata> = ArrayList()

    val noteBooksDir = Paths.get(System.getProperty("notebooksDir"))

    init {
        println("notebooks dir is " + noteBooksDir)
    }

    @RequestMapping(value = "/staticpages", method = arrayOf(RequestMethod.GET))
    fun loadStaticPages(): List<String> {
        return noteBooksDir.toFile()
                .list { f, name -> name.endsWith("html") }
                .map { it.replace(".html", "") }
    }

    @PostConstruct
    internal fun onStart() {
        storage.updateSymbolsMeta()
        storage.start()
    }

    @GetMapping(value = "/metas")
    fun loadAllMetas(): List<Metadata> {
        return allMetas
    }


    @RequestMapping(value = "/htmcontent", method = arrayOf(RequestMethod.GET))
    fun loadHtmContent(file: String): StringWrap {
        return StringWrap(String(Files.readAllBytes(noteBooksDir.resolve("$file.html"))))
    }


    @PutMapping("/put_meta")
    fun addMetadata(@Valid @RequestParam metadata: Metadata) {
        allMetas.add(metadata)
    }

    @RequestMapping(value = "/instruments", method = arrayOf(RequestMethod.GET))
    fun instruments(): Collection<InstrId> {
        return storage.meta().filter { s -> s.source != VantageDownloader.SOURCE }
    }

    @RequestMapping(value = "/timeframes", method = arrayOf(RequestMethod.GET))
    fun timeframes(): List<String> {
        return Interval.values().map { it.name }
    }


    @PostMapping("/get_ohlcs")
    fun getOhlcs(@RequestBody @Valid instrId: InstrId, interval: String): Collection<Ohlc> {
        return storage.read(instrId, interval)
    }

    @PostMapping("/get_annotations")
    fun getAnnotations(@RequestBody @Valid instrId: InstrId, interval: String): Annotations {
        val ohlcs = storage.read(instrId, interval)
        return AnnotationCreator.createAnnotations(ohlcs)
    }

    @PostMapping("/get_series")
    fun getSeries(@RequestBody @Valid codes: Array<InstrId>, interval: String): Map<String, Collection<TimePoint>> {
        val mm = HashMap<String, Collection<TimePoint>>()
        Arrays.stream(codes).forEach { c ->
            mm[c.code] = storage.read(c, interval).map { oh -> TimePoint(oh.dateTime(), oh.close) }.sorted()
        }
        return mm
    }

    @Bean
    fun customImplementation(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                //here is the line that you need
                .directModelSubstitute(LocalDateTime::class.java, String::class.java)
                .directModelSubstitute(LocalDate::class.java, String::class.java)
                .select()
                .apis(RequestHandlerSelectors.any())

                .paths(PathSelectors.regex("/.*"))
                .build()
                .pathMapping("/")
    }
}