package com.funstat.store

import com.funstat.domain.InstrId
import firelib.common.core.SourceName
import firelib.common.interval.Interval
import firelib.domain.Ohlc

interface MdStorage {

    fun meta(): List<InstrId>
    fun read(instrId: InstrId, interval: Interval): List<Ohlc>

    fun save(code: String, source: SourceName, interval: Interval, data: List<Ohlc>)

    fun updateRequested(code: String)
}


