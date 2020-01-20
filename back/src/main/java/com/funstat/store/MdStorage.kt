package com.funstat.store

import com.funstat.domain.InstrId
import firelib.domain.Ohlc

interface MdStorage {

    fun meta(): List<InstrId>
    fun read(instrId: InstrId, interval: String): List<Ohlc>

    fun save(code: String, source: String, interval: String, data: List<firelib.domain.Ohlc>)

    fun updateSymbolsMeta()

    fun start()
    fun updateRequested(code: String)
}


