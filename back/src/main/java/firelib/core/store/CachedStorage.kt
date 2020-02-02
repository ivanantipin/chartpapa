package firelib.core.store

import firelib.core.domain.InstrId
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc

class CachedStorage(private val delegate: MdStorage) : MdStorage {
    override fun updateRequested(code: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    internal val container = SingletonsContainer()

    override fun read(instrId: InstrId, interval: Interval): List<Ohlc> {
        val iv = interval
        return container.getWithExpiration(instrId.toString() + "/" + interval, { delegate.read(instrId, interval) }, Math.min(iv.duration.toMinutes() / 2, 30))

    }

    override fun save(code: String, source: SourceName, interval: Interval, data: List<firelib.core.domain.Ohlc>) {
        throw RuntimeException("not implemented")
    }

    override fun meta(): List<InstrId> {
        return delegate.meta()
    }
}
