package firelib.common.reader

import firelib.common.config.InstrumentConfig
import firelib.common.config.ModelBacktestConfig
import firelib.common.reader.binary.BinaryReaderRecordDescriptor
import firelib.common.reader.binary.OhlcDesc
import firelib.domain.Ohlc
import firelib.domain.Timed
import firelib.parser.CsvParser
import firelib.parser.LegacyMarketDataFormatLoader
import firelib.parser.ParseHandler
import firelib.parser.ParserHandlersProducer
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

class ReaderFactoryImpl(val modelConfig : ModelBacktestConfig) : ReadersFactory {

    val cachedService = CachedService(modelConfig.dataServerRoot + "/cache")

    private val ohlcDescr = OhlcDesc()


    private fun createReader(cfg : InstrumentConfig, factory : ()->Ohlc, cacheDesc : BinaryReaderRecordDescriptor<Ohlc>) : MarketDataReader<Ohlc>{
        val path: Path = Paths.get(modelConfig.dataServerRoot, cfg.path)
        //FIXME resolve format.properties first
        val iniFile: String = path.getParent().resolve("common.ini").toAbsolutePath().toString()
        val generator: ParserHandlersProducer = ParserHandlersProducer(LegacyMarketDataFormatLoader.load(iniFile))
        val ret: CsvParser<Ohlc> = CsvParser<Ohlc>(path.toAbsolutePath().toString(), generator.handlers as Array<ParseHandler<Ohlc>>, factory)
        if(modelConfig.precacheMarketData){
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

    override fun invoke(t: InstrumentConfig, startDtGmt: Instant): MarketDataReader<Timed> {
        val parser = createReader(t, {Ohlc()}, ohlcDescr)
        assert(parser.seek(startDtGmt), {"failed to find start date " + startDtGmt})
        return parser
    }
}
