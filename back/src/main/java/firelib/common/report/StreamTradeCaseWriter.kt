package firelib.common.report

import firelib.common.Trade
import firelib.common.misc.StreamTradeCaseGenerator
import java.io.BufferedOutputStream
import java.io.File.separator
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE


class StreamTradeCaseWriter(val path : Path, val factors : Iterable<String>) : (Trade)->Unit{

    val stream =  OutputStreamWriter(BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND)))

    val generatorMap = HashMap<String,StreamTradeCaseGenerator>()

    fun writeHeader(): Unit {
        stream.write((TradeSerializer().getHeader().union(factors)).joinToString(separator, postfix = "\n"))
    }

    override fun invoke(trade : Trade): Unit {
        val gen=generatorMap.getOrDefault(trade.security(),StreamTradeCaseGenerator())
        val serializer = TradeSerializer()
        for(c in gen(trade)){
            stream.write((serializer.serialize(c).union(factors.map({c.first.tradeStat.factors[it]})))
                    .joinToString (separator, postfix = "\n"))
        }
        stream.flush()
    }
}