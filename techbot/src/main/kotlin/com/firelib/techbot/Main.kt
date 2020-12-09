package com.firelib.techbot

import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.firelib.techbot.command.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.MenuReg
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import firelib.core.store.GlobalConstants
import firelib.core.store.MdStorageImpl
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileOutputStream
import java.time.Instant


const val debug_token = "1379427551:AAH-U5kTFhHHZBAJkPl4c2QuUNF8zsl17X0"

fun main(args: Array<String>) {
    initDatabase()


    if (System.getenv("TELEGRAM_TOKEN") != null) {
        startMd()
    }
    transaction {
        if (SensitivityConfig.selectAll().count() == 0L) {
            println("updating senses")
            updateSensitivties()
        }
        if (LevelSensitivityConfig.selectAll().count() == 0L) {
            println("updating level senses")
            UpdateLevelsSensitivities.updateLevelSenses()
        }

    }

    val menuReg = MenuReg()
    menuReg.makeMenu()

    menuReg.registerHandler(SubHandler())

    menuReg.registerHandler(RmHandler())

    menuReg.registerHandler(DemarkCommand())

    menuReg.registerHandler(LevelsCommand())

    menuReg.registerHandler(TrendsCommand())

    menuReg.registerHandler(RmTfHandler())

    menuReg.registerHandler(TfHandler())

    val bot = bot {
        token = System.getenv("TELEGRAM_TOKEN") ?: debug_token
        timeout = 30
        logLevel = LogLevel.Network.Body
        dispatch {
            text(null) {
                var cmd = if (text == MenuReg.mainMenu) "HOME" else text
                menuReg.map.getOrDefault(cmd, { a, b -> })(this.bot, this.message.chat.id)
            }
            callbackQuery(null) {
                menuReg.processData(this.callbackQuery.data, bot, update)
            }
        }


    }
    UsersNotifier.start(bot)
    bot.startPolling()
}

fun startMd() {
    Thread({
        val storage = MdStorageImpl()
        timeSequence(Instant.now(), Interval.Min10, 10_000L).forEach {
            try {
                SymbolsDao.available().forEach {
                    measureAndLogTime("update market data for instrument ${it.code}") {
                        storage.updateMarketData(it, Interval.Min10)
                    }
                }
            } catch (e: Exception) {
                mainLogger.error("failed to update market data", e)
            }
        }
    }).start()
}

fun makeBot(taBot: TABot): Bot {
    val bt = bot {
        token = System.getenv("TELEGRAM_TOKEN") ?: debug_token
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            text(null) {
                taBot.handle(text, bot, update)
            }
        }
    }
    return bt
}


fun initDatabase() {
    Database.connect(
        "jdbc:sqlite:${GlobalConstants.metaDb.toAbsolutePath()}?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )
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

        SchemaUtils.create(TimeFrames)
        SchemaUtils.createMissingTablesAndColumns(TimeFrames)


    }
}

fun saveFile(bytes: ByteArray, fileName: String) {
    FileOutputStream(fileName).use {
        it.write(bytes)
    }
}