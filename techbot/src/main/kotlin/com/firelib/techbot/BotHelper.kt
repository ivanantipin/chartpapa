package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.store.MdStorageImpl
import firelib.iqfeed.IntervalTransformer
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object BotHelper {
    fun displaySubscriptions(uid: Int): String {
        return transaction {
            val header = "*Ваши подписки на сигналы*\n"
            val resp = Subscriptions.select {
                Subscriptions.user eq uid
            }.map { it[Subscriptions.ticker] }.sorted().joinToString(separator = "\n")
            header + resp
        }
    }

    fun displayTimeFrames(uid: Int): String {
        return transaction {
            val header = "*Ваши таймфреймы*\n"
            val resp = TimeFrames.select {
                TimeFrames.user eq uid
            }.map { it[TimeFrames.tf] }.sorted().joinToString(separator = "\n")
            header + resp
        }
    }

    fun getSubscriptions(uid: Int): List<InstrId> {
        return transaction {
            Subscriptions.select {
                Subscriptions.user eq uid
            }.withDistinct().flatMap {
                val ret = MdService.instrByCodeAndMarket[it[Subscriptions.ticker] to it[Subscriptions.market]]
                if(ret == null){
                    println("error failed to map ${it}")
                    emptyList()
                }else{
                    listOf(ret)
                }
            }

        }
    }

    fun getTimeFrames(uid: Int): List<String> {
        return transaction {
            TimeFrames.select {
                TimeFrames.user eq uid
            }.withDistinct().map { it[TimeFrames.tf] }
        }
    }

    fun ensureExist(user: User) {
        updateDatabase("user update") {
            if (Users.select { Users.userId eq user.id }.count() == 0L) {
                Users.insert {
                    it[userId] = user.id
                    it[name] = user.firstName
                    it[familyName] = user.lastName ?: "NA"
                }
                TimeFrame.values().forEach { tff ->
                    TimeFrames.insert {
                        it[this.user] = user.id.toInt()
                        it[tf] = tff.name
                    }
                }
            }
        }
    }

    val mdStorageImpl = MdStorageImpl()

    fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval): List<Ohlc> {
        return getOhlcsForTf(ticker, timeFrame, BotConfig.window.toInt())
    }

    fun getOhlcsForTf(ticker: InstrId, timeFrame: Interval, window: Int): List<Ohlc> {
        val startTime = LocalDateTime.now().minus(timeFrame.duration.multipliedBy(window.toLong()))
        val ohlcs = mdStorageImpl.read(ticker, Interval.Min10, startTime)
        return IntervalTransformer.transform(timeFrame, ohlcs)
    }

    fun checkTicker(ticker: String, bot: Bot, update: Update): Boolean {
        if (MdService.liveSymbols.find { it.code.equals(ticker, true) } == null) {
            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = "invalid ticker ${ticker}",
                parseMode = ParseMode.MARKDOWN
            )
            return false
        }
        return true
    }

}