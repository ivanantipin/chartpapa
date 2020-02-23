package com.firelib.prod

import com.firelib.transaq.TrqHistoricalSource
import com.firelib.transaq.TrqRealtimeReaderFactory
import com.firelib.transaq.makeDefaultStub
import firelib.core.domain.Interval

fun main() {
    val fac = TrqRealtimeReaderFactory(makeDefaultStub(), Interval.Min10, populateMapping(trqMapperWriter()) {
        TrqHistoricalSource(
            makeDefaultStub(), "1"
        ).symbols()
    })
    val reader = fac.makeReader("SBER")
    while (true) {
        val peek = reader.peek()
        if (peek != null) {
            println(reader.poll())
        }
    }
}