package firelib.common

import firelib.common.timeseries.RingBuffer
import org.junit.Assert._
import org.junit.{Assert, Test}

class HistoryCircularTest {


    @Test
    def TestRingBuffer() = {
        val bufferPreInited = new RingBuffer[Array[Long]](3, () => Array[Long](0))
        var nl = bufferPreInited.add(Array(1l))
        assertEquals(1L, bufferPreInited(0)(0))
        nl = bufferPreInited.add(Array(2l))
        Assert.assertEquals(1L, bufferPreInited(1)(0))
        Assert.assertEquals(2L, bufferPreInited(0)(0))
        nl = bufferPreInited.add(Array(3l))
        nl = bufferPreInited.add(Array(4l))
        Assert.assertEquals(2L, bufferPreInited(2)(0))
        Assert.assertEquals(4L, bufferPreInited(0)(0))
    }

}
