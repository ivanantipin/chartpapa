package com.firelib.techbot

import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileOutputStream


const val debug_token = "1379427551:AAH-U5kTFhHHZBAJkPl4c2QuUNF8zsl17X0"

fun main(args: Array<String>) {
    initDatabase()
    transaction {
        if(SensitivityConfig.selectAll().count() == 0L){
            println("updating senses")
            updateSensitivties()
        }
    }
    val taBot = TABot()
    val bot = makeBot(taBot)
    bot.startPolling()
}

fun makeBot(taBot: TABot): Bot {
    val bot = bot {
        token = System.getenv("TELEGRAM_TOKEN") ?:  debug_token
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

fun saveFile(bytes: ByteArray, fileName : String) {
    FileOutputStream(fileName).use {
        it.write(bytes)
    }
}