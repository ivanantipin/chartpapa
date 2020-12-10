package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.store.MdDaoContainer
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


    fun getSubscriptions(uid: Int): List<String> {
        return transaction {
            Subscriptions.select {
                Subscriptions.user eq uid
            }.withDistinct().map { it[Subscriptions.ticker] }

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

    fun getOhlcsForTf(ticker: String, timeFrame: Interval): List<Ohlc> {
        val startTime = LocalDateTime.now().minus(timeFrame.duration.multipliedBy(BotConfig.window))
        val ohlcs = MdDaoContainer().getDao(SourceName.FINAM, Interval.Min10).queryAll(ticker, startTime)
        return IntervalTransformer.transform(timeFrame, ohlcs)
    }

    fun getOhlcsForTf(ticker: String, timeFrame: Interval, window: Int): List<Ohlc> {
        val startTime = LocalDateTime.now().minus(timeFrame.duration.multipliedBy(window.toLong()))
        val ohlcs = MdDaoContainer().getDao(SourceName.FINAM, Interval.Min10).queryAll(ticker, startTime)
        return IntervalTransformer.transform(timeFrame, ohlcs)
    }


    fun checkTicker(ticker: String, bot: Bot, update: Update): Boolean {
        if (SymbolsDao.available().find { it.code.equals(ticker, true) } == null) {
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