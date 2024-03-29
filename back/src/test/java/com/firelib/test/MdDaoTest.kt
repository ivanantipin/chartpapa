package com.firelib.test

import firelib.core.domain.Ohlc
import firelib.core.misc.SqlUtils
import firelib.core.store.MdDao
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.time.Instant


class TestMdDao{

    @Test
    fun testMdDao(){

        val file = "./test.db"

        val ff = File(file)
        if(ff.exists()){
            FileUtils.forceDelete(ff)
        }
        val mdDao = MdDao(SqlUtils.getDsForFile(file))
        val ohlc = Ohlc(Instant.now(), 0.0, 1.0, 0.5, 0.1, interpolated = false)
        val tickerName = "testTicker"
        mdDao.insertOhlc(listOf(ohlc), tickerName);
        val ohlcs = mdDao.queryAll(tickerName)
        Assert.assertEquals(1, ohlcs.size)
        Assert.assertEquals(ohlc,mdDao.queryLast(tickerName))
        Assert.assertEquals(ohlc,ohlcs[0])

    }

}