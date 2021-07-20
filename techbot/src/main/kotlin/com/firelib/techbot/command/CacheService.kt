package com.firelib.techbot.command

import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction

object CacheService{
    fun getCached(key : String, supplier : ()->ByteArray, ttlMs : Long) : ByteArray{
        return transaction {
            val rr = CacheTable.select { CacheTable.key eq key }.firstOrNull()
            if (rr == null || rr.get(CacheTable.expiryTime) < System.currentTimeMillis()) {
                CacheTable.deleteWhere { CacheTable.key eq key }
                val byteArr = supplier()
                CacheTable.insert {
                    it[CacheTable.key] = key
                    it[CacheTable.payload] = ExposedBlob(byteArr)
                    it[CacheTable.expiryTime] = System.currentTimeMillis() + ttlMs
                }
                byteArr
            } else {
                rr.get(CacheTable.payload).bytes
            }
        }

    }
}