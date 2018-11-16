package firelib.common.reader.binary

import java.nio.ByteBuffer
import java.time.Instant

import firelib.domain.Tick

class TickDesc extends BinaryReaderRecordDescriptor[Tick]{
    override def write(tick: Tick, buffer: ByteBuffer): Unit = {
        buffer.putDouble(tick.getLast)
        buffer.putInt(tick.getVol)
        buffer.putDouble(tick.getBid)
        buffer.putDouble(tick.getAsk)
        buffer.putLong(tick.getDtGmt.toEpochMilli)
        buffer.putInt(tick.getTickNumber)
    }

    def newInstance : Tick = new Tick()

    override def read(buff: ByteBuffer): Tick = {
        val tick = new Tick()
        tick.last = buff.getDouble()
        tick.vol = buff.getInt
        tick.bid = buff.getDouble()
        tick.ask = buff.getDouble()
        tick.dtGmt = Instant.ofEpochMilli(buff.getLong())
        tick.tickNumber = buff.getInt()
        tick
    }
}

