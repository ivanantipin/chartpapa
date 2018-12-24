package firelib.common.report

import firelib.common.misc.dbl2Str
import firelib.common.misc.toStandardString
import firelib.domain.Ohlc
import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class OhlcStreamWriter(val path: Path) : (Ohlc) -> Unit {

    val colsDef = listOf(
            make("DT") { it.dtGmtEnd.toStandardString() },
            make("O") { dbl2Str(it.open, 5) },
            make("H") { dbl2Str(it.high, 5) },
            make("L") { dbl2Str(it.low, 5) },
            make("C") { dbl2Str(it.close, 5) }
    )


    fun make(name: String, ff: (Ohlc) -> String): Pair<String, (Ohlc) -> String> {
        return Pair(name, ff)
    }


    val stream = OutputStreamWriter(BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)))

    init {
        stream.write(colsDef.map({ it.first }).joinToString(ReportConsts.separator, postfix = "\n"))
    }


    override fun invoke(ohlc: Ohlc) {
        stream.write(colsDef.map({ it.second }).map({ it(ohlc) }).joinToString(";", postfix = "\n"))
        stream.flush()
    }
}