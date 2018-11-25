package firelib.common.reader.binary

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.time.Instant

import firelib.common.reader.MarketDataReader
import firelib.domain.Timed

class BinaryReader<T : Timed>(val fileName : String, val desc : BinaryReaderRecordDescriptor<T>) : MarketDataReader<T>{

    val fileChannel = RandomAccessFile(fileName, "r").getChannel()

    val recLen : Int

    val buffer: ByteBuffer;

    init {
        val tmpBuff: ByteBuffer = ByteBuffer.allocate(200)
        desc.write(desc.newInstance(), tmpBuff)
        recLen = tmpBuff.position()
        buffer = ByteBuffer.allocateDirect(recLen*1000000)
        buffer.position(buffer.limit())
    }



    override fun seek(time: Instant): Boolean {
        if(endTime().isBefore(time)){
            return false
        }
        roughSeekApprox(time)
        buffer.clear()
        prebuffer()
        while (read()) {
            if (time.compareTo(current().time()) <= 0) {
                return true
            }
        }
        return false
    }

    private fun roughSeekApprox(time: Instant): Unit {
        var ppos: Long = 0
        val inc: Int = recLen*1000000

        var first = startTime()

        while (first.isBefore(time) && ppos < fileChannel.size()) {
            fileChannel.position(ppos)
            val buffer = ByteBuffer.allocateDirect(recLen)
            val len = fileChannel.read(buffer)
            buffer.flip()
            first = readBuff(buffer).time()
            ppos += inc
        }
        ppos -= 2*inc
        ppos = Math.max(0,ppos)
        fileChannel.position(ppos)
    }


    override fun endTime(): Instant {
        fileChannel.position(fileChannel.size() - recLen)
        val buff = ByteBuffer.allocateDirect(1000)
        fileChannel.read(buff)
        buff.flip()
        return readBuff(buff).time()
    }

    override fun read(): Boolean {
        if(buffer.position() == buffer.limit()){
            if(fileChannel.position() == fileChannel.size()){
                curr = null as T
                return false
            }
            buffer.clear()
            prebuffer()
        }
        curr=readBuff(buffer)
        return true
    }

    private fun prebuffer(): Unit {
        buffer.clear()
        fileChannel.read(buffer)
        buffer.flip()
    }

    fun readBuff(buff : ByteBuffer): T {
        return desc.read(buff)
    }


    override fun startTime(): Instant {
        fileChannel.position(0)
        val buff = ByteBuffer.allocateDirect(recLen)
        fileChannel.read(buff)
        buff.flip()
        return readBuff(buff).time()
    }

    private var curr : T? = null

    override fun current(): T = curr!!

    override fun close(): Unit {
        fileChannel.close()
    }
}