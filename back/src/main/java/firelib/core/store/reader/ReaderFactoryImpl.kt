package firelib.core.store.reader

import firelib.core.store.reader.binary.BinaryReaderRecordDescriptor
import firelib.core.store.reader.binary.OhlcDesc
import firelib.core.domain.Ohlc
import firelib.parser.CsvParser
import firelib.parser.LegacyMarketDataFormatLoader
import firelib.parser.ParseHandler
import firelib.parser.ParserHandlersProducer
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

class ReaderFactoryImpl(val dsPath : String, val precache: Boolean = false){

    val cachedService = CachedService("$dsPath/cache")

    private val ohlcDescr = OhlcDesc()


    private fun createReader(path : String, factory : ()->Ohlc, cacheDesc : BinaryReaderRecordDescriptor<Ohlc>) : MarketDataReader<Ohlc>{
        val path: Path = Paths.get(dsPath, path)
        //FIXME resolve format.properties first
        val iniFile: String = path.getParent().resolve("common.ini").toAbsolutePath().toString()
        val generator = ParserHandlersProducer(LegacyMarketDataFormatLoader.load(iniFile))
        val ret: CsvParser<Ohlc> = CsvParser<Ohlc>(path.toAbsolutePath().toString(), generator.handlers as Array<ParseHandler<Ohlc>>, factory)
        if(precache){
            val reader = cachedService.checkPresent(path.toAbsolutePath().toString(), ret.startTime(), ret.endTime(), cacheDesc)
            if(reader != null){
                return reader
            }else{
                return cachedService.write(path.toAbsolutePath().toString(),ret,cacheDesc)
            }
        }else{
            return ret
        }
    }

    fun create(t: String, startDtGmt: Instant): MarketDataReader<Ohlc> {
        val parser = createReader(t, {Ohlc(interpolated = false) }, ohlcDescr)
        assert(parser.seek(startDtGmt), { "failed to find start date $startDtGmt" })
        return parser
    }
}
