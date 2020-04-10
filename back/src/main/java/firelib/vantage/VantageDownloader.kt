package firelib.vantage

import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.store.MdDao
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


class VantageDownloader : HistoricalSource {

    internal var mdDao: MdDao? = null

    override fun symbols(): List<InstrId> {
        /*
        fixme
                List<InstrId> ret = mdDao.readGeneric("vantage_symbols", InstrId.class);
        ret.add(new InstrId("RASP.MOS","RASP.MOS", MICEX,"RASP.MOS", SOURCE));
        return ret;

         */
        return ArrayList()
    }

    override fun load(instrId: InstrId): Sequence<Ohlc> {
        val template = RestTemplate()

        val url = "https://www.alphavantage.co/query"
        val function = "TIME_SERIES_DAILY"
        val apiKey = "P28H4WI1MIPJPGBP"
        val dataType = "csv"


        val request = (url +
                "?function=" + function
                + "&instrId=" + instrId.code
                + "&apikey=" + apiKey
                + "&datatype=" + dataType)


        val entity = template.getForEntity(request, String::class.java)
        return entity.body!!.split("\r\n").map { oh -> parse(oh) }.filter { oh -> oh != null }.map { it!! }.asSequence()

    }

    override fun load(instrId: InstrId, dateTime: LocalDateTime): Sequence<Ohlc> {
        return load(instrId)
    }

    override fun getName(): SourceName {
        return SourceName.VANTAGE
    }

    override fun getDefaultInterval(): Interval {
        return Interval.Day
    }

    companion object {

        val SOURCE = SourceName.VANTAGE
        internal var pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd")


        @JvmStatic
        fun main(args: Array<String>) {

            val load = VantageDownloader().load(
                InstrId(
                    "RASP.MOS",
                    "RASP.MOS",
                    "RASP.MOS",
                    "RASP.MOS",
                    "RASP.MOS"
                )
            )
            println(load)

        }

        fun parse(str: String): Ohlc? {
            try {
                val arr = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                return Ohlc(LocalDate.parse(arr[0], pattern).atStartOfDay().toInstant(ZoneOffset.UTC),
                        java.lang.Double.parseDouble(arr[1]),
                        java.lang.Double.parseDouble(arr[2]), java.lang.Double.parseDouble(arr[3]), java.lang.Double.parseDouble(arr[4]), 0, 0, false)
            } catch (e: Exception) {
                println("not valid entry " + str + " because " + e.message)
                return null
            }

        }
    }


}
