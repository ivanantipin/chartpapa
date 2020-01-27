package com.funstat.store

import firelib.common.misc.atUtc
import firelib.common.reader.SimplifiedReader
import firelib.domain.Ohlc
import java.time.Instant
import java.util.*


class SimplifiedReaderImpl(val mdDao: MdDao, val code : String, val startTime : Instant) :
    SimplifiedReader {

    var lastRead : Instant = startTime

    var buffer  = LinkedList<Ohlc>()

    fun read(){
        val list = mdDao.queryAll(code, lastRead.atUtc(), 20_000)
        if(list.isNotEmpty()){
            buffer.addAll(list)
            lastRead = list.last().endTime
        }
    }

    override fun peek(): Ohlc? {
        if(buffer.isEmpty()){
            read()
        }
        return buffer.peek()
    }

    override fun poll(): Ohlc {
        return buffer.poll()
    }

}