package com.firelib.techbot.command

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

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

    @Synchronized
    fun getCached(key: String, supplier: () -> ByteArray, ttlMs: Long): ByteArray {
        return transaction(cache) {
            val rr = CacheTable.select { CacheTable.key eq key }.firstOrNull()
            if (rr == null || rr.get(CacheTable.expiryTime) < System.currentTimeMillis()) {
                CacheTable.deleteWhere { CacheTable.key eq key }
                val byteArr = supplier()
                CacheTable.insert {
                    it[CacheTable.key] = key
                    it[payload] = ExposedBlob(byteArr)
                    it[expiryTime] = System.currentTimeMillis() + ttlMs
                }
                byteArr
            } else {
                rr.get(CacheTable.payload).bytes
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