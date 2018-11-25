package firelib.common.reader.binary

import java.nio.ByteBuffer
import java.time.Instant

import firelib.domain.Ohlc

class OhlcDesc : BinaryReaderRecordDescriptor<Ohlc>{
    override fun write(tick: Ohlc, buffer: ByteBuffer): Unit {
        buffer.putDouble(tick.O)
        buffer.putDouble(tick.H)
        buffer.putDouble(tick.L)
        buffer.putDouble(tick.C)
        buffer.putInt(tick.Volume)
        buffer.putLong(tick.dtGmtEnd.toEpochMilli())
        buffer.putChar(if(tick.interpolated) 'I' else 'R')
    }

    override fun newInstance (): Ohlc = Ohlc()

    override fun read(buff: ByteBuffer): Ohlc {
        val ret = Ohlc(O = buff.getDouble(),
                H = buff.getDouble(),
                L = buff.getDouble(),
                C = buff.getDouble(),
                Volume = buff.getInt(),
                dtGmtEnd = Instant.ofEpochMilli(buff.getLong())
        )
        ret.interpolated = (buff.getChar() == 'I')
        return ret
    }
}