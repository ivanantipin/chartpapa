package firelib.common.reader.binary

import java.nio.ByteBuffer
import java.time.Instant

import firelib.domain.Ohlc

class OhlcDesc extends BinaryReaderRecordDescriptor[Ohlc]{
    override def write(tick: Ohlc, buffer: ByteBuffer): Unit = {
        buffer.putDouble(tick.getO)
        buffer.putDouble(tick.getH)
        buffer.putDouble(tick.getL)
        buffer.putDouble(tick.getC)
        buffer.putInt(tick.getVolume)
        buffer.putLong(tick.dtGmtEnd.toEpochMilli)
        buffer.putChar(if(tick.interpolated) 'I' else 'R')
    }

    def newInstance : Ohlc = new Ohlc()

    override def read(buff: ByteBuffer): Ohlc = {
        val ohlc = new Ohlc()
        ohlc.O = buff.getDouble()
        ohlc.H = buff.getDouble()
        ohlc.L = buff.getDouble()
        ohlc.C = buff.getDouble()
        ohlc.Volume = buff.getInt()
        ohlc.dtGmtEnd = Instant.ofEpochMilli(buff.getLong())
        ohlc.interpolated = (buff.getChar == 'I')
        ohlc
    }
}
