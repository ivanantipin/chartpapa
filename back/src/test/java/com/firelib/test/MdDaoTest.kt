package com.firelib.test

import firelib.store.MdDao
import firelib.common.core.report.SqlUtils
import firelib.domain.Ohlc
import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.io.File
import java.time.Instant


class TestMdDao{

    @Test
    @Ignore
    fun testMdDao(){

        val file = "./test.db"

        val ff = File(file)
        if(ff.exists()){
            FileUtils.forceDelete(ff)
        }
        val mdDao = MdDao(SqlUtils.getDsForFile(file))
        val ohlc = Ohlc(Instant.now(), 0.0, 1.0, 0.5, 0.1)
        val tickerName = "testTicker"
        mdDao.insertOhlc(listOf(ohlc), tickerName);
        val ohlcs = mdDao.queryAll(tickerName)
        Assert.assertEquals(1, ohlcs.size)
        Assert.assertEquals(ohlc,ohlcs[0])
        Assert.assertEquals(ohlc,mdDao.queryLast(tickerName).get())
    }

}