package com.firelib.techbot

import com.firelib.techbot.command.CmdLine
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
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.LocalDateTime

object BotHelper {
    fun displaySubscriptions(uid: Int): String {
        val header = "*Your subscriptions*\n"
        val resp = Subscriptions.select {
            Subscriptions.user eq uid
        }.map {
            "${it[Subscriptions.ticker]} : ${it[Subscriptions.timeframe]}"
        }.sorted().joinToString(separator = "\n")
        return header + resp
    }

    fun ensureExist(user : User){
        if (Users.select { Users.userId eq user.id }.count() == 0L) {
            Users.insert {
                it[userId] = user.id
                it[name] = user.firstName
                it[familyName] = user.lastName ?: "NA"
            }
        }
    }

    fun getUsage(cmd : Any) : String{
        val outStr = ByteArrayOutputStream()
        CommandLine.usage(cmd, PrintStream(outStr))
        return String(outStr.toByteArray())
    }

    fun parseCommand(subCmd : CmdLine, args : List<String>, bot : Bot, update : Update) : Boolean{
        try {
            CommandLine(subCmd).setCaseInsensitiveEnumValuesAllowed(true). parseArgs(*args.toTypedArray())
            subCmd.postConstruct()
        }catch (e : Exception){
            e.printStackTrace()
            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = getUsage(subCmd),
                parseMode = ParseMode.MARKDOWN
            )
            return false
        }
        return true
    }

    fun getOhlcsForTf(ticker : String, timeFrame: Interval) : List<Ohlc>{
        val startTime = LocalDateTime.now().minus(timeFrame.duration.multipliedBy(BotConfig.window))
        val ohlcs = MdDaoContainer().getDao(SourceName.FINAM, Interval.Min10).queryAll(ticker, startTime)
        return IntervalTransformer.transform(timeFrame, ohlcs)
    }

    fun getOhlcsForTf(ticker : String, timeFrame: Interval, window : Int) : List<Ohlc>{
        val startTime = LocalDateTime.now().minus(timeFrame.duration.multipliedBy(window.toLong()))
        val ohlcs = MdDaoContainer().getDao(SourceName.FINAM, Interval.Min10).queryAll(ticker, startTime)
        return IntervalTransformer.transform(timeFrame, ohlcs)
    }



    fun checkTicker(ticker : String, bot : Bot, update : Update): Boolean {
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