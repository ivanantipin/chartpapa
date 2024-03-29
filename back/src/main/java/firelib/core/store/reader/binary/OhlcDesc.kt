package firelib.core.store.reader.binary

import firelib.core.domain.Ohlc
import java.nio.ByteBuffer
import java.time.Instant

class OhlcDesc : BinaryReaderRecordDescriptor<Ohlc>{
    override fun write(tick: Ohlc, buffer: ByteBuffer): Unit {
        buffer.putDouble(tick.open)
        buffer.putDouble(tick.high)
        buffer.putDouble(tick.low)
        buffer.putDouble(tick.close)
        buffer.putLong(tick.volume)
        buffer.putLong(tick.endTime.toEpochMilli())
        buffer.putChar(if(tick.interpolated) 'I' else 'R')
    }

    override fun newInstance (): Ohlc = Ohlc()

    override fun read(buff: ByteBuffer): Ohlc {
        val ret = Ohlc(endTime = Instant.ofEpochMilli(buff.getLong()),
                open = buff.getDouble(),
                high = buff.getDouble(),
                low = buff.getDouble(),
                close = buff.getDouble(),
                volume = buff.getLong()
        )
        ret.interpolated = (buff.getChar() == 'I')
        return ret
    }
}