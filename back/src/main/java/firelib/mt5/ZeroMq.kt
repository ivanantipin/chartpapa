package firelib.mt5

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.store.MdStorageImpl
import firelib.core.store.SourceFactory


val module = ObjectMapper().registerModule(KotlinModule())


fun main() {
    val fac = SourceFactory()
    val storageImpl = MdStorageImpl()
    //storageImpl.updateMarketData(InstrId(code = "ALLFUTSi", source = SourceName.MT5.name), Interval.Min15)
    updateMt5(fac, storageImpl)
}

private fun updateMt5(fac: SourceFactory, storageImpl: MdStorageImpl) {
//    val symbols = fac[SourceName.IQFEED].symbols()

    val symbols = MT5Source().symbols()

    println(symbols)

//    storageImpl.updateMarketData(InstrId(code = "STL", source = SourceName.MT5.name), Interval.Min30)

    var counter = 0

    symbols.map {
        it.copy(source = SourceName.MT5.name)
    }.forEach {
        println("updating ${counter++} out of ${symbols.size}")
        storageImpl.updateMarketData(it, Interval.Min30)
    }
}