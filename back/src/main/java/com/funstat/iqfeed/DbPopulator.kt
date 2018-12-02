package com.funstat.iqfeed

import com.funstat.store.MdStorageImpl
import firelib.common.interval.Interval
import firelib.domain.Ohlc
import firelib.parser.*

import java.nio.file.Paths
import java.time.Instant
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicLong

object DbPopulator {

    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val syncer = MdStorageImpl(MdStorageImpl.HOME_PATH)
        ssync(syncer)
    }

    fun ssync(storage: MdStorageImpl) {
        val dir = Paths.get("/ddisk/globaldatabase/1MIN/STK")
        val cnt = AtomicLong()
        val iniFile = dir.resolve("common.ini").toAbsolutePath().toString()

        val load = LegacyMarketDataFormatLoader.load(iniFile)
        val producer = ParserHandlersProducer(load)

        for (s in dir.toFile().list { f, b -> b.endsWith("csv") }!!) {
            try {
                val table = s.replace(".csv", "").replace("_1", "")

                val fname = dir.resolve(s).toAbsolutePath().toString()
                val ohlcs = ArrayList<Ohlc>()
                try {
                    val parser = CsvParser(fname, producer.handlers as Array<out ParseHandler<Ohlc>>, { Ohlc() }, 100000000)
                    println(parser.seek(Instant.MIN))
                    while (parser.read()) {
                        ohlcs.add(parser.current())
                        cnt.incrementAndGet()
                    }
                    storage.save(table, "IQFEED", Interval.Min1.name, ohlcs)
                    println("done $fname cnt is $cnt")

                } catch (e: Exception) {
                    println("failed " + s + " due to " + e.message)
                    e.printStackTrace()
                }

            } catch (e: Exception) {
                println("failed for " + s + " due to " + e.message)
            }

        }
    }
}
