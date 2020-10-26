package com.firelib.techbot

import com.firelib.sub.BreachEvents
import com.firelib.sub.Subscriptions
import com.firelib.sub.Users
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.echo.chart.SensitivityConfig
import com.github.kotlintelegrambot.echo.com.firelib.telbot.BreachEvent
import com.github.kotlintelegrambot.echo.com.firelib.telbot.BreachEventKey
import com.github.kotlintelegrambot.echo.com.firelib.telbot.TABot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.UsersNotifier
import firelib.core.domain.Interval
import firelib.core.misc.UtilsHandy
import firelib.core.misc.timeSequence
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.time.Instant


fun makeBot(taBot: TABot): Bot {
    val bot = bot {
        token = "1379427551:AAH-U5kTFhHHZBAJkPl4c2QuUNF8zsl17X0"
        dispatch {
            text { bot, update ->
                val text = update.message?.text ?: "Hello, World!"
                taBot.handle(text, bot, update)
            }
        }
    }
    return bot
}

fun initDatabase(){
    Database.connect("jdbc:sqlite:/tmp/chatbot.db", driver = "org.sqlite.JDBC")

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
        SchemaUtils.create(SensitivityConfig)
        SchemaUtils.create(Subscriptions)
        SchemaUtils.create(BreachEvents)
        SchemaUtils.createMissingTablesAndColumns(Subscriptions)
        SchemaUtils.createMissingTablesAndColumns(Users)
        SchemaUtils.createMissingTablesAndColumns(SensitivityConfig)

    }

}


fun main(args: Array<String>) {

    initDatabase()


    val taBot = TABot()


    val bot = makeBot(taBot)

//    GlobalScope.launch {
//        timeSequence(Instant.now(), Interval.Min10, 10_000L).forEach {
//            try {
//                UtilsHandy.updateRussianDivStocks(interval = Interval.Min10)
//            }catch (e : Exception){
//                e.printStackTrace()
//            }
//        }
//    }

    bot.startPolling()
}


fun saveFile(bytes: ByteArray, fileName : String) {
    FileOutputStream(fileName).use {
        it.write(bytes)
    }
}