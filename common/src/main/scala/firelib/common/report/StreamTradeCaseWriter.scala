package firelib.common.report

import java.io.{BufferedOutputStream, OutputStreamWriter}
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}

import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator

import scala.collection.mutable

class StreamTradeCaseWriter(val path : Path, val factors : Iterable[String]) extends TradeSerializer with (Trade=>Unit){

    val stream =  new OutputStreamWriter(new BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND)))

    val generatorMap = new mutable.HashMap[String,StreamTradeCaseGenerator]

    def writeHeader(): Unit = {
        stream.write((getHeader() ++ factors).mkString(separator) :+ '\n')
    }

    override def apply(trade : Trade): Unit = {
        val gen=generatorMap.getOrElseUpdate(trade.security,new StreamTradeCaseGenerator)
        for(c <- gen(trade)){
            stream.write((serialize(c) ++ factors.map(c._1.tradeStat.factors(_))).mkString(separator) :+ '\n')
        }
        stream.flush()
    }
}
