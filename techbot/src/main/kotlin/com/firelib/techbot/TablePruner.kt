package com.firelib.techbot

import firelib.core.domain.Interval
import firelib.core.store.MdStorageImpl
import java.time.Instant

class TablePruner {

    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val storageImpl = MdStorageImpl()
            val secInDay = 24*3600
            initDatabase()
            MdService.liveSymbols.forEach {
                println(it)
                storageImpl.deleteSince(it, Interval.Min10, Instant.now().minusSeconds(7*secInDay.toLong()))
            }
        }
    }
}