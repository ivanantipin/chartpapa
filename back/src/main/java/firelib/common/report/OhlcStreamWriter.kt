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
            make("DT", { o -> o.dtGmtEnd.toStandardString() }),
            make("O", { o -> dbl2Str(o.open, 5) }),
            make("H", { o -> dbl2Str(o.high, 5) }),
            make("L", { o -> dbl2Str(o.low, 5) }),
            make("C", { o -> dbl2Str(o.close, 5) })
    )


    fun make(name: String, ff: (Ohlc) -> String): Pair<String, (Ohlc) -> String> {
        return Pair(name, ff)
    }


    val stream = OutputStreamWriter(BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)))

    init {
        stream.write(colsDef.map({ it.first }).joinToString(ReportConsts.separator, postfix = "\n"))
    }


    override fun invoke(ohlc: Ohlc): Unit {
        stream.write(colsDef.map({ it.second }).map({ it(ohlc) }).joinToString(";", postfix = "\n"))
        stream.flush()
    }
}