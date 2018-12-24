package com.firelib.test

import firelib.common.opt.OptimizedParameter
import firelib.common.opt.ParamsVariator
import junit.framework.Assert.assertEquals
import org.junit.Assert
import org.junit.Test


class VariatorTest {

    @Test
    fun TestOptVariator()  {
        val variator = ParamsVariator(listOf(
                OptimizedParameter("p0", 0, 2),
        OptimizedParameter("p1", 0, 3)
        ))

        assertEquals(6, variator.combinations())



        var set = mutableSetOf(
                Pair(0, 0),
                Pair(0, 1),
                Pair(1, 0),
                Pair(1, 1),
                Pair(0, 2),
                Pair(1, 2))


        while (variator.hasNext()) {
            val dd = variator.next()
            var key = Pair(dd["p0"]!!, dd["p1"]!!)
            Assert.assertTrue(set.contains(key))
            set.remove(key)
        }
        assertEquals(0, set.size)
    }

}
