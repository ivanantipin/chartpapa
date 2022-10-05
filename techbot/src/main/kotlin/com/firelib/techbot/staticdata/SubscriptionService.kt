package com.firelib.techbot.staticdata

import com.firelib.techbot.UserId
import com.firelib.techbot.persistence.SourceSubscription
import com.firelib.techbot.updateDatabase
import firelib.core.domain.InstrId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class SubscriptionService(val staticDataService: StaticDataService) {

    val subscriptions = ConcurrentHashMap<UserId, ConcurrentHashMap<String, InstrId>>()


    val listeners = CopyOnWriteArrayList<(InstrId)->Unit>()

    fun liveInstruments() : List<InstrId>{
        return subscriptions.values.flatMap { it.values }.distinct()
    }


    fun addListener(ll : (InstrId)->Unit){
        listeners += ll
    }

    fun deleteSubscription(userId: UserId, instrId: InstrId) : Boolean{
        val rows = updateDatabase("insert subscription") {
            SourceSubscription.deleteWhere {
                (SourceSubscription.sourceId eq instrId.id) and (SourceSubscription.user eq userId.id)
            }
        }.get()
        return rows > 0
    }

    fun addSubscription(userId: UserId, instrId: InstrId) : Boolean{
        val putToCache = putToCache(userId, instrId)
        if (putToCache == null) {
            updateDatabase("insert subscription") {
                SourceSubscription.insert {
                    it[sourceId] = instrId.id
                    it[sourceName] = instrId.source
                    it[user] = userId.id
                }
            }
            listeners.forEach { it(instrId) }
            return true
        }
        return false
    }

    private fun putToCache(userId: UserId, instrId: InstrId): InstrId? {
        return subscriptions.computeIfAbsent(userId, { ConcurrentHashMap() }).put(instrId.id, instrId)
    }

    fun start() {
        transaction {
            SourceSubscription.selectAll().forEach { rr ->
                val ret = staticDataService.id2inst[rr[SourceSubscription.sourceId]]
                if (ret != null) {
                    putToCache(UserId(rr[SourceSubscription.user]), ret)
                }
            }
        }
    }

}