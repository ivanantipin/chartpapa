package firelib.core.store.reader.binary

import java.nio.ByteBuffer

interface BinaryReaderRecordDescriptor<T>{
    fun write(t : T, bb : ByteBuffer)
    fun read (bb : ByteBuffer) : T
    fun newInstance (): T
}