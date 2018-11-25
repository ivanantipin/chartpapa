package firelib.common.reader.binary

import java.io.*
import java.nio.ByteBuffer

class BinaryWriter<T>(val fileName: String, val desc: BinaryReaderRecordDescriptor<T>) {

    val aFile: File = File(fileName)
    val fileChannel = RandomAccessFile(aFile, "rw").getChannel()

    val buffer: ByteBuffer = ByteBuffer.allocateDirect(1000)

    fun write(tick: T): Unit {
        buffer.clear()
        desc.write(tick, buffer)
        buffer.flip()
        fileChannel.write(buffer)
    }

    fun flush(): Unit {
        fileChannel.close()
    }
}