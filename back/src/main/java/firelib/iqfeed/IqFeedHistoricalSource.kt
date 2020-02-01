package firelib.iqfeed

import firelib.domain.InstrId
import firelib.common.core.HistoricalSource
import firelib.common.core.SourceName
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.domain.Ohlc
import firelib.domain.date
import firelib.domain.ret
import firelib.parser.CsvParser
import firelib.parser.LegacyMarketDataFormatLoader
import firelib.parser.ParseHandler
import firelib.parser.ParserHandlersProducer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

class IqFeedHistoricalSource(val csvPath: Path) : HistoricalSource {

    override fun symbols(): List<InstrId> {
        val map = code2name()
        return csvPath.toFile().list().map {
            it.replace("_1.csv", "")
        }.filter { map.containsKey(it) }.map {
            InstrId(
                it,
                map!![it]!!,
                "NA",
                it,
                SOURCE.name
            )
        }
    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        return load(instrId, LocalDateTime.now().minusDays(600));
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {
        val iniFile = csvPath.resolve("common.ini").toAbsolutePath().toString()
        val load = LegacyMarketDataFormatLoader.load(iniFile)
        val producer = ParserHandlersProducer(load)
        val fname = csvPath.resolve(instrId.code + "_1.csv").toAbsolutePath().toString()

        return sequence {
            val parser = CsvParser<Ohlc>(fname, producer.handlers as Array<out ParseHandler<Ohlc>>?, { Ohlc() }, 10_000_000)
            println(parser.seek(dateTime.toInstant(ZoneOffset.UTC)))
            while (parser.read()) {
                yield(parser.current())
            }
        }
    }

    override fun getName(): SourceName {
        return SourceName.IQFEED
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Min1
    }

    companion object {
        val SOURCE = SourceName.IQFEED
        fun code2name(): Map<String, String> {
            try {
                val lines = Files.readAllLines(Paths.get(IqFeedHistoricalSource::class.java.getResource("/iqfeed_symbols.txt").toURI()))
                return lines.map { it.split(";") }.associateBy({ it[0] }, { it[1] })
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println(IqFeedHistoricalSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")).symbols())
        }
    }
}

fun solve(data : Array<MutableList<Double>>) : Array<Double>{
    return emptyArray()
}

fun main(args: Array<String>) {
    val minBars = IqFeedHistoricalSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")).load(
        InstrId(
            code = "SPY"
        )
    )
            .filter { it.endTime.atUtc().toLocalTime().hour != 0 }
            .toList()

    val tenMins = IntervalTransformer.transform(Interval.Min10, minBars)

    var currentDate = LocalDate.now()

    val arr = Array(30,{ i-> mutableListOf<Double>()})

    var solution = emptyArray<Double>()

    tenMins.forEach({

        if(it.date() != currentDate){
            if(arr[0].size == 30){
                solution = solve(arr)
            }
            currentDate = it.date()
        }

        var idx = it.endTime.atUtc().toLocalTime().toSecondOfDay()/600 - 100
        arr[idx].add(it.ret())
        if(arr[idx].size > 30){
            arr[idx].removeAt(0);
        }
    })
}