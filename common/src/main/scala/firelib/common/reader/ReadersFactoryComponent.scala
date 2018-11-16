package firelib.common.reader

import java.nio.file.{Path, Paths}
import java.time.Instant
import java.util.function.Supplier

import firelib.common.MarketDataType
import firelib.common.config.InstrumentConfig
import firelib.common.core.ModelConfigContext
import firelib.common.reader.binary.{BinaryReaderRecordDescriptor, OhlcDesc, TickDesc}
import firelib.domain.{Ohlc, Tick, Timed}
import firelib.parser.{CsvParser, LegacyMarketDataFormatLoader, ParseHandler, ParserHandlersProducer}

trait ReadersFactoryComponent {

    this : ModelConfigContext =>

    val readersFactory : ReadersFactory = new ReaderFactoryImpl()

    class ReaderFactoryImpl() extends ReadersFactory {

        val cachedService = new CachedService(modelConfig.dataServerRoot + "/cache")


        private val ohlcFactory = new Supplier[Ohlc]{
            override def get(): Ohlc = return new Ohlc()
        }

        private val tickFactory = new Supplier[Tick]{
            override def get(): Tick = return new Tick()
        }

        private val tickDescr = new TickDesc

        private val ohlcDescr = new OhlcDesc


        private def createReader[T <: Timed](cfg : InstrumentConfig, factory : Supplier[T], cacheDesc : BinaryReaderRecordDescriptor[T]) : MarketDataReader[T] ={
            val path: Path = Paths.get(modelConfig.dataServerRoot, cfg.path)
            //FIXME resolve format.properties first
            val iniFile: String = path.getParent.resolve("common.ini").toAbsolutePath.toString
            val generator: ParserHandlersProducer = new ParserHandlersProducer(LegacyMarketDataFormatLoader.load(iniFile))
            val ret: CsvParser[T] = new CsvParser[T](path.toAbsolutePath.toString, generator.handlers.asInstanceOf[Array[ParseHandler[T]]], factory)
            if(modelConfig.precacheMarketData){
                cachedService.checkPresent(path.toAbsolutePath.toString, ret.startTime(),ret.endTime(),cacheDesc) match{
                    case Some(reader)=>reader.asInstanceOf[MarketDataReader[T]]
                    case None=>{
                        cachedService.write(path.toAbsolutePath.toString,ret,cacheDesc)
                    }
                }
            }else{
                ret
            }
        }

        override def apply(t: InstrumentConfig, startDtGmt: Instant): MarketDataReader[Timed] = {
            val parser = if (t.mdType == MarketDataType.Tick)
                    createReader[Tick](t, tickFactory, tickDescr)
                else
                    createReader[Ohlc](t, ohlcFactory, ohlcDescr)
            assert(parser.seek(startDtGmt), "failed to find start date " + startDtGmt)
            parser
        }
    }

}
