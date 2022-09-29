package com.firelib.techbot

import chart.SignalType
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.JsonHelper
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.IntervalTransformer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

fun ChatId.getId(): Long {
    return (this as ChatId.Id).id
}

object BotHelper {

    fun displayTimeFrames(uid: Update): String {
        return transaction {
            val header = Msg.YourTimeframes
            val resp = TimeFrames.select {
                TimeFrames.user eq uid.fromUser().id
            }.map { it[TimeFrames.tf] }.sorted().joinToString(separator = "\n")
            header.toLocal(uid.langCode()) + resp
        }
    }

    fun getSubscriptions(uid: Long): List<InstrId> {
        return transaction {
            Subscriptions.select {
                Subscriptions.user eq uid
            }.withDistinct().flatMap {
                val ret = MdService.instrByCodeAndMarket[it[Subscriptions.ticker] to it[Subscriptions.market]]
                if (ret == null) {
                    deleteUnmappedSubscription(it)
                    emptyList()
                } else {
                    listOf(ret)
                }
            }
        }
    }

    private fun deleteUnmappedSubscription(it: ResultRow) {
        mainLogger.error("failed to map ${it} deleting")
        Subscriptions.deleteWhere { Subscriptions.ticker eq it[Subscriptions.ticker] and (Subscriptions.market eq it[Subscriptions.market]) }
    }

    fun getSubscriptions(): Map<UserId, List<InstrId>> {
        return transaction {
            Subscriptions.selectAll().flatMap {
                val ret = MdService.instrByCodeAndMarket[it[Subscriptions.ticker] to it[Subscriptions.market]]
                if (ret == null) {
                    deleteUnmappedSubscription(it)
                    emptyList()
                } else {
                    listOf(UserId(it[Subscriptions.user].toLong()) to ret)
                }
            }.groupBy({ it.first }, { it.second })
        }
    }

    fun getTimeFrames(): Map<UserId, List<TimeFrame>> {
        return transaction {
            TimeFrames.selectAll().map {
                UserId(it[TimeFrames.user].toLong()) to TimeFrame.valueOf(it[TimeFrames.tf])
            }.groupBy({ it.first }, { it.second })
        }
    }

    fun getSignalTypes(): Map<UserId, List<SignalType>> {
        return transaction {
            SignalTypes.selectAll().map {
                UserId(it[SignalTypes.user].toLong()) to SignalType.valueOf(it[SignalTypes.signalType])
            }.groupBy({ it.first }, { it.second })
        }
    }

    fun getAllSettings(): Map<UserId, List<Map<String, String>>> {
        return transaction {
            Settings.selectAll().map {
                UserId(it[Settings.user].toLong()) to JsonHelper.fromJson<Map<String, String>>(it[Settings.value])
            }.groupBy({ it.first }, { it.second })
        }
    }

    fun readSettings(userId: Long): List<Map<String, String>> {
        return transaction {
            Settings.select { (Settings.user eq userId) }.map {
                JsonHelper.fromJson(it[Settings.value])
            }
        }
    }

    fun getTimeFrames(uid: ChatId): List<String> {
        return transaction {
            TimeFrames.select {
                TimeFrames.user eq uid.getId()
            }.withDistinct().map { it[TimeFrames.tf] }
        }
    }

    fun getSignalTypes(uid: ChatId): List<SignalType> {
        return transaction {
            SignalTypes.select {
                SignalTypes.user eq uid.getId()
            }.withDistinct().map { SignalType.valueOf(it[SignalTypes.signalType]) }
        }
    }

    fun ensureExist(user: User) {
        updateDatabase("user update") {

            val llang = try {
                Langs.valueOf(user.languageCode!!).name
            } catch (e: Exception) {
                Langs.EN.name
            }


            if (Users.select { Users.userId eq user.id }.count() == 0L) {
                Users.insert {
                    it[userId] = user.id
                    it[name] = user.firstName
                    it[familyName] = user.lastName ?: "NA"
                    it[lang] = llang
                }
                TimeFrame.values().forEach { tff ->
                    TimeFrames.insert {
                        it[this.user] = user.id
                        it[tf] = tff.name
                    }
                }
                SignalType.values().filter { it != SignalType.MACD }.forEach { tff ->
                    SignalTypes.insert {
                        it[this.user] = user.id
                        it[this.signalType] = tff.name
                    }
                }
            }
        }
    }

    fun saveFile(bytes: ByteArray, fileName: String) {
        File(fileName).parentFile.mkdirs()
        FileOutputStream(fileName).use {
            it.write(bytes)
        }
    }
}

