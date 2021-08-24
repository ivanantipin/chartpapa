package com.firelib.techbot.command

import firelib.common.initDatabase
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

object CacheService{

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(CacheTable)
        }
    }

    fun getCached(key : String, supplier : ()->ByteArray, ttlMs : Long) : ByteArray{
        return transaction {
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
    initDatabase()
    transaction {
        CacheService.getCached("some", { "initial".toByteArray() }, 1000)
        Thread.sleep(500)
        val updated = CacheService.getCached("some", { "update0".toByteArray() }, 1000)
        println(String(updated))
        Thread.sleep(600)
        val updated1 = CacheService.getCached("some", { "update1".toByteArray() }, 1000)
        println(String(updated1))

    }
}