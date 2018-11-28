package com.funstat.store

import com.funstat.domain.InstrId
import com.funstat.domain.Ohlc
import firelib.common.interval.Interval

class CachedStorage(private val delegate: MdStorage) : MdStorage {

    internal val container = SingletonsContainer()

    override fun read(instrId: InstrId, interval: String): List<Ohlc> {
        val iv = Interval.valueOf(interval)
        return container.getWithExpiration(instrId.toString() + "/" + interval, { delegate.read(instrId, interval) }, iv.duration.toMinutes() / 2)

    }

    override fun save(code: String, source: String, interval: String, data: List<firelib.domain.Ohlc>) {
        throw RuntimeException("not implemented")
    }

    override fun meta(): List<InstrId> {
        return delegate.meta()
    }

    override fun updateSymbolsMeta() {
        delegate.updateSymbolsMeta()
    }
}
