package firelib.common.report
import java.io.BufferedOutputStream
import java.io.OutputStreamWriter
import java.nio.file.StandardOpenOption.*
import java.nio.file.Files
import java.nio.file.Path

import firelib.common.Order
import firelib.common.report.OrderSerializer.Companion.getHeader
import firelib.common.report.OrderSerializer.Companion.serialize
import firelib.common.report.ReportConsts.Companion.separator

class StreamOrderWriter(val path : Path) : ((Order)->Unit){

    val stream =  OutputStreamWriter(BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND)))

    fun writeHeader(): Unit {
        stream.write(getHeader().joinToString(separator= separator))
        stream.write("\n")
    }

    override fun invoke(order : Order): Unit {
        stream.write(serialize(order).joinToString(separator = separator))
        stream.write("\n")
        stream.flush()
    }
}