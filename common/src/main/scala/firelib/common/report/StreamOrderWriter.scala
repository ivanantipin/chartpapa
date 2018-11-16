package firelib.common.report
import java.io.{BufferedOutputStream, OutputStreamWriter}
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path}

import firelib.common.Order

class StreamOrderWriter(val path : Path) extends OrderSerializer with (Order=>Unit){

    val stream =  new OutputStreamWriter(new BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND)))

    def writeHeader(): Unit = {
        stream.write(getHeader() :+ '\n')
    }

    override def apply(order : Order): Unit = {
        stream.write(serialize(order) :+ '\n')
        stream.flush()
    }
}
