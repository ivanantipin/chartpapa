package com.firelib.techbot.command

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

data class CacheRecord(val key : String, val expiryTime : Long, val time : Long, val data : ByteArray)

object CacheService {

    val cache = Database.connect(
        "jdbc:sqlite:/tmp/cache.db?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )

    init {
        transaction(cache) {
            SchemaUtils.create(CacheTable)
            SchemaUtils.createMissingTablesAndColumns(CacheTable)
        }
    }


    fun getCached(key: String, supplier: () -> ByteArray, ttlMs: Long): ByteArray {
        return getCacheRecord(key, supplier, ttlMs).data

    }

    @Synchronized
    fun getCacheRecord(key: String, supplier: () -> ByteArray, ttlMs: Long): CacheRecord {
        return transaction(cache) {
            val rr = getRecord(key)
            val time = System.currentTimeMillis()
            val expiryTime = time + ttlMs
            if (rr == null || rr.expiryTime < time) {
                val record = CacheRecord(key, expiryTime, time, supplier())
                updateRecord(record)
                record
            } else {
                rr
            }
        }
    }

    @Synchronized
    fun updateRecord(record : CacheRecord){
        transaction(cache) {
            CacheTable.deleteWhere { CacheTable.entryKey eq record.key }
            CacheTable.insert {
                it[CacheTable.entryKey] = record.key
                it[payload] = ExposedBlob(record.data)
                it[CacheTable.time] = record.time
                it[CacheTable.expiryTime] = record.expiryTime
            }
        }
    }

    @Synchronized
    fun getRecord(key : String) : CacheRecord?{
        return transaction(cache) {
            val rr = CacheTable.select { CacheTable.entryKey eq key }.firstOrNull()
            if(rr == null){
                null
            }else{
                CacheRecord(key, rr.get(CacheTable.expiryTime), rr.get(CacheTable.time), rr.get(CacheTable.payload).bytes)
            }
        }
    }
}

fun main() {
    CacheService.getCached("some", { "initial".toByteArray() }, 1000)
    Thread.sleep(500)
    val updated = CacheService.getCached("some", { "update0".toByteArray() }, 1000)
    println(String(updated))
    Thread.sleep(600)
    val updated1 = CacheService.getCached("some", { "update1".toByteArray() }, 1000)
    println(String(updated1))
}