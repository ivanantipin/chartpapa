package com.funstat.store

import firelib.domain.Ohlc
import com.funstat.domain.InstrId
import firelib.common.interval.Interval

interface MdStorage {

    fun meta(): List<InstrId>
    fun read(instrId: InstrId, interval: String): List<Ohlc>

    fun save(code: String, source: String, interval: String, data: List<firelib.domain.Ohlc>)

    fun updateSymbolsMeta()
}


