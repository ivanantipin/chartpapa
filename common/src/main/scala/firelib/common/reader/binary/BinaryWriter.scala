package firelib.common.reader.binary

import java.io._
import java.nio.ByteBuffer

class BinaryWriter[T](val fileName : String, val desc : BinaryReaderRecordDescriptor[T]){

    val aFile: File = new File(fileName)
    val fileChannel = new RandomAccessFile(aFile, "rw").getChannel

    val buffer: ByteBuffer = ByteBuffer.allocateDirect(1000)

    def write(tick : T): Unit ={
        buffer.clear()
        desc.write(tick,buffer)
        buffer.flip()
        fileChannel.write(buffer)
    }

    def flush(): Unit ={
        fileChannel.close()
    }
}
