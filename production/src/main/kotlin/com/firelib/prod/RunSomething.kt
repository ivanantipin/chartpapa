package com.firelib.prod

import com.firelib.transaq.TrqHistoricalSource
import com.firelib.transaq.TrqMsgDispatcher
import com.firelib.transaq.TrqRealtimeReaderFactory
import com.firelib.transaq.makeDefaultStub
import firelib.core.domain.Interval
import firelib.core.store.DbMapper
import firelib.core.store.trqMapperWriter

fun main() {
//    val symbols = TrqHistoricalSource(makeDefaultStub(), "1").symbols()
//
//    trqMapperWriter().write(symbols)
//
//    println("count is ${symbols.size}")
    val dbMapper = DbMapper(trqMapperWriter(), { true })
    val fac = TrqRealtimeReaderFactory(TrqMsgDispatcher((makeDefaultStub())), Interval.Sec10, dbMapper)
    val reader = fac.makeReader("SRH0")
    while (true){
        if(reader.peek() == null){
            Thread.sleep(1000)
        }else{
            println(reader.poll())
        }

    }


}