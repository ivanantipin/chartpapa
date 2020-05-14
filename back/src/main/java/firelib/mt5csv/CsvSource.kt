package firelib.mt5csv

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import java.io.File
import java.io.FileReader
import java.lang.Double
import java.nio.charset.Charset
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


val mt5format = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm")

fun parseMt5(str: String): Ohlc? {
    try {
        val arr = str.split(",").toTypedArray()
        return Ohlc(
            LocalDateTime.parse(arr[0], mt5format).toInstant(ZoneOffset.UTC),
            Double.parseDouble(arr[1]),
            Double.parseDouble(arr[2]), Double.parseDouble(arr[3]), Double.parseDouble(arr[4]), 0, arr[6].toLong(), false)
    } catch (e: Exception) {
        println("not valid entry " + str + " because " + e.message)
        return null
    }
}


class CsvSource : HistoricalSource {
    override fun symbols(): List<InstrId> {
        return File("/home/ivan/transaq/md/").list().map {
            InstrId(code = it!!)
        }
    }

    override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {

        val suff = when(interval){
            Interval.Min15, Interval.Min30 ->"M${interval.duration.toMinutes()}"
            Interval.Min240 ->"4H"
            else->"NA"
        }

        val reade = FileReader(
            "/home/ivan/transaq/md/${instrId.code}${suff}.csv",
            Charset.forName("unicode")
        )
        return sequence<Ohlc> {
            yieldAll(reade.readLines().map({ parseMt5(it)!!}))
        }
    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {
        return load(instrId, interval)
    }

    override fun getName(): SourceName {
        return SourceName.MT5
    }

}