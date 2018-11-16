package firelib.common.reader.binary

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.time.Instant

import firelib.common.reader.MarketDataReader
import firelib.domain.Timed

class BinaryReader[T <: Timed](val fileName : String, desc : BinaryReaderRecordDescriptor[T]) extends MarketDataReader[T]{

    val fileChannel = new RandomAccessFile(fileName, "r").getChannel

    val recLen = {
        val tmpBuff: ByteBuffer = ByteBuffer.allocate(200)
        desc.write(desc.newInstance, tmpBuff)
        tmpBuff.position()
    }


    val buffer: ByteBuffer = ByteBuffer.allocateDirect(recLen*1000000)

    buffer.position(buffer.limit())

    def seek(time: Instant): Boolean = {
        if(endTime().isBefore(time)){
            return false
        }
        roughSeekApprox(time)
        buffer.clear()
        prebuffer()
        while (read) {
            if (time.compareTo(current.time) <= 0) {
                return true
            }
        }
        return false
    }

    private def roughSeekApprox(time: Instant): Unit = {
        var ppos: Long = 0
        val inc: Int = recLen*1000000

        var first = startTime()

        while (first.isBefore(time) && ppos < fileChannel.size()) {
            fileChannel.position(ppos)
            val buffer = ByteBuffer.allocateDirect(recLen)
            val len: Long = fileChannel.read(buffer)
            buffer.flip()
            first = readBuff(buffer).time
            ppos += inc
        }
        ppos -= 2*inc
        ppos = Math.max(0,ppos)
        fileChannel.position(ppos)
    }


    override def endTime(): Instant = {
        fileChannel.position(fileChannel.size() - recLen)
        val buff = ByteBuffer.allocateDirect(1000)
        fileChannel.read(buff)
        buff.flip()
        readBuff(buff).time
    }

    override def read(): Boolean = {
        if(buffer.position() == buffer.limit()){
            if(fileChannel.position() == fileChannel.size()){
                curr = null.asInstanceOf[T]
                return false
            }
            buffer.clear()
            prebuffer()
        }
        curr=readBuff(buffer)
        return true
    }

    private def prebuffer(): Unit ={
        buffer.clear()
        fileChannel.read(buffer)
        buffer.flip()
    }

    def readBuff(buff : ByteBuffer): T ={
        return desc.read(buff)
    }


    override def startTime(): Instant = {
        fileChannel.position(0)
        val buff = ByteBuffer.allocateDirect(recLen)
        fileChannel.read(buff)
        buff.flip()
        readBuff(buff).time
    }

    private var curr : T =_

    override def current: T = curr

    override def close(): Unit = {
        fileChannel.close()
    }
}
