package com.firelib.test

import firelib.core.domain.Ohlc
import firelib.core.misc.atUtc
import firelib.indicators.sequenta.Sequenta
import firelib.indicators.sequenta.SequentaSignalType
import org.junit.Assert
import org.junit.Test
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class SeqTest {

    @Test
    fun testSequenta() {
        val pattern = DateTimeFormatter.ofPattern("yyyy.MM.dd")


        val ohlcs = InputStreamReader(SeqTest::class.java.getResourceAsStream("/goog_for_seq.txt")).readLines().map {
            val arr = it.split(',')
            Ohlc(
                LocalDate.parse(arr[0], pattern).atStartOfDay().toInstant(ZoneOffset.UTC),
                java.lang.Double.parseDouble(arr[1]),
                java.lang.Double.parseDouble(arr[2]),
                java.lang.Double.parseDouble(arr[3]),
                java.lang.Double.parseDouble(arr[4]),
                0,
                0,
                false
            )
        }

        data class SigSig(
            val date : LocalDate,
            val type : SequentaSignalType,
            val recRation : Double?,
            val count : Int,
            val tdst : Double?

        )


        val seq = Sequenta()


        val siggi = ohlcs.flatMap {
            val signals = seq.onOhlc(it)
            signals.filter { it.type == SequentaSignalType.Signal || it.type == SequentaSignalType.SetupReach }
                .map {
                    SigSig(
                        date = seq.data.last().endTime.atUtc().toLocalDate(),
                        type = it.type,
                        recRation = it.reference.recycleRatio(),
                        count = it.reference.completedSignal,
                        tdst = it.reference?.tdst
                    )
                }
        }

        Assert.assertEquals("" +
                "[SigSig(date=2019-12-16, type=SetupReach, recRation=null, count=-1, tdst=1304.87), " +
                "SigSig(date=2020-01-14, type=SetupReach, recRation=null, count=-1, tdst=1341.55), " +
                "SigSig(date=2020-01-17, type=Signal, recRation=2.3241310493929777, count=13, tdst=1304.87), " +
                "SigSig(date=2020-02-10, type=Signal, recRation=2.688009313154828, count=21, tdst=1304.87), " +
                "SigSig(date=2020-02-19, type=Signal, recRation=null, count=13, tdst=1341.55)]", siggi.toString())

        println(siggi)

    }
}