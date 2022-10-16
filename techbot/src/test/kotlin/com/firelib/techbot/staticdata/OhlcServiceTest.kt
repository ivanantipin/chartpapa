package com.firelib.techbot.staticdata

import com.firelib.techbot.persistence.DbHelper
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.SqlUtils
import firelib.core.misc.toInstantDefault
import firelib.core.store.MdDao
import firelib.iqfeed.ContinousOhlcSeries
import org.junit.Assert
import org.junit.Test
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import kotlin.random.Random

class OhlcServiceTest {

    fun makeRandomOhlc(time: Instant): Ohlc {
        val open = Random.nextDouble(5.0, 10.0)
        val high = open + Random.nextDouble(1.0, 3.0)
        val low = open - Random.nextDouble(1.0, 3.0)
        return Ohlc(
            endTime = time,
            open = open,
            high = high,
            low = low,
            close = (high + low) / 2.0,
            volume = Random.nextLong(5, 100),
            interpolated = false
        )
    }

    @Test
    fun ohlcServiceTest() {

        DbHelper.initDatabase(Paths.get("/tmp/${System.currentTimeMillis()}_meta.db"))


        mutableListOf<Int>().add(0, 0)

        val ticker = "A${System.currentTimeMillis()}"

        val instr = InstrId(id = ticker, market = "XNAS", code = ticker, source = SourceName.POLIGON.name)

        val dsForFile = SqlUtils.getDsForFile("/tmp/${System.currentTimeMillis()}.db")

        val dao = MdDao(dsForFile)

        val historicalSource = object : HistoricalSource {
            var ohlcs: List<Ohlc>
            override fun symbols(): List<InstrId> {
                return listOf(instr)
            }

            init {
                val startTime = Interval.Min10.roundTime(Instant.now())
                ohlcs = (0..10).map {
                    makeRandomOhlc(startTime.plusSeconds(it * 60 * 10L))
                }
            }

            override fun load(instrId: InstrId, interval: Interval): Sequence<Ohlc> {
                TODO("Not yet implemented")
            }

            override fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Sequence<Ohlc> {

                val ret = ohlcs.filter { it.endTime > dateTime.toInstantDefault() }
                println("start loading ${dateTime} interval ${interval} ohlcs ${ret}")
                return ret.asSequence()
            }

            override fun getName(): SourceName {
                return SourceName.DUMMY
            }
        }

        val service = OhlcsService({ a, b -> dao }, { historicalSource })

        service.initTimeframeIfNeeded(instr)

        Assert.assertEquals(historicalSource.ohlcs, service.getOhlcsForTf(instr, Interval.Min10))

        val series = ContinousOhlcSeries(Interval.Min30)
        series.add(historicalSource.ohlcs)

        val tf = service.getOhlcsForTf(instr, Interval.Min30)
        Assert.assertEquals(series.data, tf)

    }
}