package com.firelib.techbot.subscriptions

import com.firelib.techbot.domain.UserId
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.staticdata.InstrumentsService
import firelib.core.domain.InstrId
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class SubscriptionService(val staticDataService: InstrumentsService) {

    val subscriptions = ConcurrentHashMap<UserId, ConcurrentHashMap<String, InstrId>>()

    val listeners = CopyOnWriteArrayList<(InstrId) -> Unit>()

    init {
        initialLoad()
    }

    fun liveInstruments(): List<InstrId> {
        return subscriptions.values.flatMap { it.values }.distinct()
    }

    fun addListener(ll: (InstrId) -> Unit) {
        listeners += ll
    }

    fun deleteSubscription(userId: UserId, instrId: InstrId): Boolean {
        val rows = DbHelper.updateDatabase("insert subscription") {
            SourceSubscription.deleteWhere {
                (SourceSubscription.sourceId eq instrId.id) and (SourceSubscription.user eq userId.id)
            }
        }.get()
        val subs = subscriptions.computeIfAbsent(userId, { ConcurrentHashMap<String, InstrId>() })
        subs.remove(instrId.id)
        return rows > 0
    }

    fun addSubscription(userId: UserId, instrId: InstrId): Boolean {
        val putToCache = putToCache(userId, instrId)
        if (putToCache == null) {
            DbHelper.updateDatabase("insert subscription") {
                SourceSubscription.insert {
                    it[sourceId] = instrId.id
                    it[sourceName] = instrId.source
                    it[user] = userId.id
                }
            }.get()
            listeners.forEach { it(instrId) }
            return true
        }
        return false
    }

    private fun putToCache(userId: UserId, instrId: InstrId): InstrId? {
        return subscriptions.computeIfAbsent(userId, { ConcurrentHashMap() }).put(instrId.id, instrId)
    }

    private fun initialLoad() {
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