package com.funstat.store

import com.funstat.domain.InstrId
import firelib.domain.Ohlc
import firelib.common.interval.Interval

class CachedStorage(private val delegate: MdStorage) : MdStorage {
    override fun updateRequested(code: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun start() {
        delegate.start()
    }

    internal val container = SingletonsContainer()

    override fun read(instrId: InstrId, interval: String): List<Ohlc> {
        val iv = Interval.valueOf(interval)
        return container.getWithExpiration(instrId.toString() + "/" + interval, { delegate.read(instrId, interval) }, Math.min(iv.duration.toMinutes() / 2, 30))

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
