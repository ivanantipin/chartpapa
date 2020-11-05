package com.firelib.techbot

import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.handlers.TextHandler
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import firelib.core.store.GlobalConstants
import firelib.core.store.MdStorageImpl
import okhttp3.logging.HttpLoggingInterceptor
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileOutputStream
import java.time.Instant


const val debug_token = "1379427551:AAH-U5kTFhHHZBAJkPl4c2QuUNF8zsl17X0"

fun main(args: Array<String>) {
    initDatabase()
    if(System.getenv("TELEGRAM_TOKEN") != null){
        startMd()
    }
    transaction {
        if(SensitivityConfig.selectAll().count() == 0L){
            println("updating senses")
            updateSensitivties()
        }
        if(LevelSensitivityConfig.selectAll().count() == 0L){
            println("updating level senses")
            UpdateLevelsSensitivities.updateLevelSenses()
        }

    }
    val taBot = TABot()
    val bot = makeBot(taBot)
    UsersNotifier.start(bot)
    bot.startPolling()
}

fun startMd(){
    Thread({
        val storage = MdStorageImpl()
        timeSequence(Instant.now(), Interval.Min10, 10_000L).forEach {
            try {
                SymbolsDao.available().forEach {
                    storage.updateMarketData(it, Interval.Min10)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }).start()
}

fun makeBot(taBot: TABot): Bot {
    val bt = bot {
        logLevel = HttpLoggingInterceptor.Level.BASIC
        token = System.getenv("TELEGRAM_TOKEN") ?:  debug_token
        dispatch {
            text { bot , update ->
                val text = update.message?.text ?: "Hello, World!"
                taBot.handle(text, bot, update)
            }
        }
    }
    return bt
}


fun initDatabase(){
    Database.connect("jdbc:sqlite:${GlobalConstants.metaDb.toAbsolutePath()}?journal_mode=WAL", driver = "org.sqlite.JDBC")
    transaction {

        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
        SchemaUtils.createMissingTablesAndColumns(Users)

        SchemaUtils.create(SensitivityConfig)
        SchemaUtils.createMissingTablesAndColumns(SensitivityConfig)

        SchemaUtils.create(Subscriptions)
        SchemaUtils.createMissingTablesAndColumns(Subscriptions)

        SchemaUtils.create(BreachEvents)
        SchemaUtils.createMissingTablesAndColumns(BreachEvents)

        SchemaUtils.create(LevelSensitivityConfig)
        SchemaUtils.createMissingTablesAndColumns(LevelSensitivityConfig)

        SchemaUtils.create(CommandsLog)
        SchemaUtils.createMissingTablesAndColumns(CommandsLog)

    }
}

fun saveFile(bytes: ByteArray, fileName : String) {
    FileOutputStream(fileName).use {
        it.write(bytes)
    }
}