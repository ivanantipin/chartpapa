package com.firelib.test

import firelib.core.timeseries.RingBuffer
import junit.framework.Assert.assertEquals
import org.junit.Test

class HistoryCircularTest {


    @Test
    fun TestRingBuffer() {
        val bufferPreInited = RingBuffer(3) { arrayOf(0L) }
        bufferPreInited.add(arrayOf(1L))
        assertEquals(1L, bufferPreInited[0][0])
        bufferPreInited.add(arrayOf(2L))
        assertEquals(1L, bufferPreInited[1][0])
        assertEquals(2L, bufferPreInited[0][0])
        bufferPreInited.add(arrayOf(3L))
        bufferPreInited.add(arrayOf(4L))
        assertEquals(2L, bufferPreInited[2][0])
        assertEquals(4L, bufferPreInited[0][0])
    }

}
