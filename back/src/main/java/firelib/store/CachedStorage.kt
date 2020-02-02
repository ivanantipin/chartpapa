package firelib.store

import firelib.domain.InstrId
import firelib.common.core.SourceName
import firelib.domain.Interval
import firelib.domain.Ohlc

class CachedStorage(private val delegate: MdStorage) : MdStorage {
    override fun updateRequested(code: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    internal val container = SingletonsContainer()

    override fun read(instrId: InstrId, interval: Interval): List<Ohlc> {
        val iv = interval
        return container.getWithExpiration(instrId.toString() + "/" + interval, { delegate.read(instrId, interval) }, Math.min(iv.duration.toMinutes() / 2, 30))

    }

    override fun save(code: String, source: SourceName, interval: Interval, data: List<firelib.domain.Ohlc>) {
        throw RuntimeException("not implemented")
    }

    override fun meta(): List<InstrId> {
        return delegate.meta()
    }
}
